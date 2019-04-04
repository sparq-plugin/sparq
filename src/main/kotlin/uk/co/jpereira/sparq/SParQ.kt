/*
 * Copyright 2019 Joao Pereira
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.jpereira.sparq

import ij.IJ
import ij.ImagePlus
import ij.WindowManager
import ij.gui.GenericDialog
import ij.gui.WaitForUserDialog
import ij.io.DirectoryChooser
import ij.plugin.ChannelSplitter
import loci.plugins.BF
import loci.plugins.`in`.ImporterOptions
import org.scijava.command.Command
import org.scijava.plugin.Plugin
import uk.co.jpereira.sparq.dialogs.*
import uk.co.jpereira.sparq.utils.removeLastEntryFromSummary
import uk.co.jpereira.sparq.utils.saveSummary
import uk.co.jpereira.sparq.utils.updateImageThreshold
import uk.co.jpereira.sparq.utils.zoomOutImage
import java.io.File

class RedoThresholdException : Exception()

@Plugin(type = Command::class, menuPath = "Plugins>SParQ")
open class SParQPlugin : Command {
    override fun run() {
        val directory = directoryToProcessImagesFrom() ?: return

        val extensions = ExtensionChooserDialog().open()
        if (extensions.isEmpty()) {
            print("Canceled because no extensions where selected")
            return
        }

        val channel = ChannelSelectorDialog().open()
        if (channel == Channel.ERROR) {
            print("Canceled while selecting channel")
            return
        }
        var numberOfProcessedFiles = 0

        /*
         * For each file in the directory the next piece of code will
         * try to process the image.
         * During the process the user can ask to reprocess the same image
         * or to skip the current image all together
         */
        directory.listFiles().forEach {
            if (extensions.contains(it.extension)) {
                while (true) {
                    try {
                        imageProcess(it, channel)
                        numberOfProcessedFiles++
                        break
                    } catch (_: RedoThresholdException) {
                        removeLastEntryFromSummary()
                        println("This should call imageProcess again")
                    }
                }
            }
        }

        WaitForUserDialog("A total of $numberOfProcessedFiles were processed, going to save results").show()

        saveResults(numberOfProcessedFiles)
    }

    /*
     * The image processing steps are:
     * 1. Open the image and split into channels
     * 2. Show the image channel the user asked for
     * 3. Display the threshold dialog to the user to select the threshold
     * 4. Display the nuclei channel to help the user to validate the current threshold
     * 5. Analyze the particles using the built in plugin ParticleAnalyzer
     * 6. Display a dialog to the user to decide if the result is the expected or not
     *    If the result is not the expected one than it will restart the image process
     *    for the current image if not it will move to the next image
     * 7. Closes all the images open
     *
     * The code bellow has the pointers for each step described above
     */
    private fun imageProcess(image: File, useChannel: Channel) {
        val (cellImage, nucleiImage) = openImage(image, useChannel) // step 1

        cellImage.show("original image") // step 2
        val cellImageOutput = thresholdImage(cellImage) ?: return //step 3

        nucleiImage.show("original image") // step 4
        zoomOutImage(nucleiImage)

        // next section is step 5
        IJ.run(cellImageOutput, "Set Measurements...", "area limit add redirect=None decimal=3")
        IJ.run(cellImageOutput, "Analyze Particles...", "size=0.50-Infinity show=[Masks] display exclude clear summarize")
        val particleImage = WindowManager.getCurrentImage()
        zoomOutImage(particleImage)
        particleImage.window.setLocation(450, 300)

        // next section is step 6
        val isFinishedDialog = GenericDialog("Finished, does it look ok?")
        isFinishedDialog.setCancelLabel("No, repeat threshold")
        isFinishedDialog.setOKLabel("Yes, move to next image")
        isFinishedDialog.showDialog()
        if (isFinishedDialog.wasCanceled()) {
            cellImage.changes = false
            cellImage.close()
            cellImageOutput.changes = false
            cellImageOutput.close()
            nucleiImage.changes = false
            nucleiImage.close()
            throw RedoThresholdException()
        }

        // next setion is step 7
        WindowManager.getCurrentImage().close()
        cellImage.changes = false
        cellImage.close()
        cellImageOutput.changes = false
        cellImageOutput.close()
        nucleiImage.changes = false
        nucleiImage.close()
    }

    private fun openImage(imageFile: File, useChannel: Channel): Pair<ImagePlus, ImagePlus> {
        val images: Array<ImagePlus>
        if (imageFile.extension != "tif") {
            val options = ImporterOptions()
            options.isSplitChannels = true
            options.id = imageFile.absolutePath
            images = BF.openImagePlus(options)
        } else {
            val tifImage = IJ.openImage(imageFile.absolutePath)
            images = ChannelSplitter.split(tifImage)
        }

        var useChannelArrayPosition = useChannel.ordinal
        var blueChannelArrayPosition = Channel.BLUE.ordinal
        if (!images[0].stack.isRGB) {
            useChannelArrayPosition = invertToBGR(useChannel)
            blueChannelArrayPosition = invertToBGR(Channel.BLUE)
        }

        val cellImage = images[useChannelArrayPosition]
        if (images.size == 2) {
            return Pair(cellImage, images[1])
        }

        return Pair(cellImage, images[blueChannelArrayPosition])
    }

    private fun thresholdImage(image: ImagePlus): ImagePlus? {
        val minThreshold = 50
        val maxThreshold = 255
        zoomOutImage(image)
        val thresholdedImage = image.duplicate()
        IJ.run(thresholdedImage, "8-bit", "")
        thresholdedImage.show("With Threshold applied")
        thresholdedImage.displayMode = IJ.GRAYSCALE
        zoomOutImage(thresholdedImage)

        thresholdedImage.window.setLocation(900, 60)
        updateImageThreshold(thresholdedImage, minThreshold, maxThreshold)
        thresholdedImage.repaintWindow()
        val dialog = ThresholdDialog(thresholdedImage)
        dialog.show(minThreshold = minThreshold.toDouble())

        if (dialog.wasCanceled()) {
            image.close()
            thresholdedImage.changes = false
            thresholdedImage.close()
            return null
        }

        image.close()
        return thresholdedImage
    }

    private fun saveResults(numberOfProcessedFiles: Int) {
        if (numberOfProcessedFiles > 0) {
            val directoryChooser = DirectoryChooser("directory to save CSV")
            val directoryPath = directoryChooser.directory
            if (directoryPath == null || directoryPath.isEmpty()) {
                WaitForUserDialog("Will not save the CSV file").isVisible = true
                return
            }
            val resultsFilePath = File(directoryChooser.directory).absolutePath + File.separator + "Results.csv"
            saveSummary(resultsFilePath)
            WaitForUserDialog("Results file saved in: $resultsFilePath")
        }
    }

    private fun directoryToProcessImagesFrom(): File? {
        val directoryChooser = DirectoryChooser("Select the folder to read images from")
        val directoryPath = directoryChooser.directory
        if (directoryPath == null || directoryPath.isEmpty())
            return null
        return File(directoryChooser.directory)
    }
}


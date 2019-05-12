/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import ij.IJ
import ij.ImagePlus
import ij.WindowManager
import ij.gui.GenericDialog
import ij.gui.WaitForUserDialog
import ij.io.DirectoryChooser
import org.scijava.command.Command
import org.scijava.plugin.Plugin
import uk.co.jpereira.sparq.dependencies.BioformatImpl
import uk.co.jpereira.sparq.dependencies.ChannelSpliterImpl
import uk.co.jpereira.sparq.dependencies.ImageJOpenImageImpl
import uk.co.jpereira.sparq.dialogs.ChannelSelectorDialog
import uk.co.jpereira.sparq.dialogs.ExtensionChooserDialog
import uk.co.jpereira.sparq.dialogs.ThresholdDialog
import uk.co.jpereira.sparq.utils.*
import java.io.File

class RedoThresholdException : Exception()

typealias ImagesToProcess = MutableMap<String, Pair<ImagePlus, ImagePlus>>

@Plugin(type = Command::class, menuPath = "Plugins>SParQ")
open class SParQPlugin : Command {
    private val bioFormatOpenImage = BioformatImpl()
    private val channelSplitter = ChannelSpliterImpl()
    private val imageJImageOpener = ImageJOpenImageImpl()

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

        val imageOpener = ImageOpener(imageJImageOpener, bioFormatOpenImage, channelSplitter)
        /*
         * For each file in the directory the next piece of code will
         * try to process the image.
         * During the process the user can ask to reprocess the same image
         * or to skip the current image all together
         */
        directory.listFiles().forEach { file ->
            if (extensions.contains(file.extension)) {
                while (true) {
                    try {
                        val imagesToProcess = imageOpener.open(file, channel)
                        imagesToProcess.forEach { images ->
                            imageProcess(images.key, images.value, channel)
                            numberOfProcessedFiles++
                        }
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
     * 1. Show the image channel the user asked for
     * 2. Display the threshold dialog to the user to select the threshold
     * 3. Display the nuclei channel to help the user to validate the current threshold
     * 4. Analyze the particles using the built in plugin ParticleAnalyzer
     * 5. Display a dialog to the user to decide if the result is the expected or not
     *    If the result is not the expected one than it will restart the image process
     *    for the current image if not it will move to the next image
     * 6. Closes all the images open
     *
     * The code bellow has the pointers for each step described above
     */
    private fun imageProcess(name: String, images: Pair<ImagePlus, ImagePlus>, useChannel: Channel) {
        val (cellImage, nucleiImage) = images // step 1

        cellImage.show("original image") // step 1
        val cellImageOutput = thresholdImage(cellImage) ?: return //step 2

        nucleiImage.show("original image") // step 3
        zoomOutImage(nucleiImage)

        // next section is step 4
        IJ.run(cellImageOutput, "Set Measurements...", "area limit add redirect=None decimal=3")
        IJ.run(cellImageOutput, "Analyze Particles...", "size=0.50-Infinity show=[Masks] display exclude clear summarize")
        val particleImage = WindowManager.getCurrentImage()
        zoomOutImage(particleImage)
        particleImage.window.setLocation(450, 300)

        // next section is step 5
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

        // next setion is step 6
        WindowManager.getCurrentImage().close()
        cellImage.changes = false
        cellImage.close()
        cellImageOutput.changes = false
        cellImageOutput.close()
        nucleiImage.changes = false
        nucleiImage.close()
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


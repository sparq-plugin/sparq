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
package uk.co.jpereira.thresholder

import ij.IJ
import ij.ImagePlus
import ij.WindowManager
import ij.gui.DialogListener
import ij.gui.GenericDialog
import ij.gui.WaitForUserDialog
import ij.io.DirectoryChooser
import ij.plugin.Thresholder
import ij.util.Tools
import loci.plugins.BF
import loci.plugins.`in`.ImporterOptions
import org.scijava.command.Command
import org.scijava.plugin.Plugin
import java.awt.AWTEvent
import java.awt.Checkbox
import java.awt.TextField
import java.io.File

class RedoThresholdException : Exception()

@Plugin(type = Command::class, menuPath = "Plugins>Analyze Images")
open class Thresholder : Command {
    override fun run() {
        var directoryChooser = DirectoryChooser("Select the folder to read images from")
        var directoryPath = directoryChooser.directory
        if (directoryPath == null || directoryPath.isEmpty())
            return
        var directory = File(directoryChooser.directory)

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

        directory.listFiles().forEach {
            if (extensions.contains(it.extension)) {
                while (true) {
                    try {
                        imageProcess(it, channel)
                        numberOfProcessedFiles++
                        break
                    } catch (_: RedoThresholdException) {
                        println("This should call imageProcess again")
                    }
                }
            }
        }

        WaitForUserDialog("A total of $numberOfProcessedFiles were processed, going to save results")

        if (numberOfProcessedFiles > 0) {
            directoryChooser = DirectoryChooser("directory to save CSV")
            directoryPath = directoryChooser.directory
            if (directoryPath == null || directoryPath.isEmpty()) {
                WaitForUserDialog("Will not save the CSV file")
                return
            }
            directory = File(directoryChooser.directory)
            IJ.selectWindow("Summary")
            IJ.saveAs("results", directory.absolutePath + File.separator + "Results.csv")
        }
    }

    private fun imageProcess(image: File, useChannel: Channel) {
        val (cellImage, nucleiImage) = openImage(image, useChannel)

        cellImage.show("original image")
        val cellImageOutput = thresholdImage(cellImage) ?: return

//        nucleiImage.show("original image")
//        val nucleiImageOutput = thresholdImage(nucleiImage)
//        if (nucleiImageOutput == null) {
//            cellImage.changes = false
//            cellImage.close()
//            cellImageOutput.changes = false
//            cellImageOutput.close()
//            return
//        }

        IJ.run(cellImageOutput, "Set Measurements...", "area limit add redirect=None decimal=3")
        IJ.run(cellImageOutput, "Analyze Particles...", "size=0.50-Infinity show=[Masks] display exclude clear summarize")

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
//            nucleiImageOutput.changes = false
//            nucleiImageOutput.close()
            throw RedoThresholdException()
        }

        WindowManager.getCurrentImage().close()
        cellImage.changes = false
        cellImage.close()
        cellImageOutput.changes = false
        cellImageOutput.close()
        nucleiImage.changes = false
        nucleiImage.close()
//        nucleiImageOutput.changes = false
//        nucleiImageOutput.close()
    }

    private fun openImage(imageFile: File, useChannel: Channel): Pair<ImagePlus, ImagePlus> {
        val options = ImporterOptions()
        options.isSplitChannels = true
        options.id = imageFile.absolutePath
        val image = BF.openImagePlus(options)
        val cellImage = image[useChannel.ordinal]
        if (image.size == 2) {
            return Pair(cellImage, image[1])
        }

        return Pair(cellImage, image[Channel.BLUE.ordinal])
    }

    private fun thresholdImage(image: ImagePlus): ImagePlus? {
        val minThreshold = 50
        val maxThreshold = 255
        IJ.run(image, "Out [-]", "")
        IJ.run(image, "Out [-]", "")
        val thresholdedImage = image.duplicate()
        IJ.run(thresholdedImage, "8-bit", "")
        thresholdedImage.show("With Threshold applied")
        thresholdedImage.displayMode = IJ.GRAYSCALE
        IJ.run(thresholdedImage, "Out [-]", "")
        IJ.run(thresholdedImage, "Out [-]", "")

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
}

enum class Channel {
    RED,
    GREEN,
    BLUE,

    ERROR
}

class ExtensionChooserDialog {
    fun open(): List<String> {
        val dialog = GenericDialog("Select files extensions to process")

        dialog.addCheckboxGroup(3, 7, arrayOf(
                "tif", "tiff", "zvi"
        ), BooleanArray(3))
        dialog.setOKLabel("Ok")
        dialog.setCancelLabel("Exit Plugin")
        dialog.showDialog()

        if (dialog.wasCanceled()) {
            return emptyList()
        }

        return dialog.checkboxes
                .filter { (it as Checkbox).state }
                .map { (it as Checkbox).label }
    }
}

class ChannelSelectorDialog {
    fun open(): Channel {
        val dialog = GenericDialog("Select channels to use")
        dialog.addChoice("Channels:", arrayOf("Red", "Green"), "Red")
        dialog.showDialog()
        if (dialog.wasCanceled()) {
            return Channel.ERROR
        }
        if (dialog.nextChoiceIndex == 0)
            return Channel.RED
        return Channel.GREEN
    }
}

class ThresholdDialog(private val image: ImagePlus) : GenericDialog("Threshold") {
    fun show(minThreshold: Double) {
        addSlider("Minimum histogram value", 0.0, 255.0, minThreshold)
        addSlider("Maximum histogram value", 0.0, 255.0, 255.0)
        addDialogListener(
                ThresholdDialogListener(
                        dialog = this,
                        image = image,
                        minThreshold = minThreshold,
                        maxThreshold = 255.0
                ))
        showDialog()
    }
}

class ThresholdDialogListener(
        private val dialog: ThresholdDialog,
        private val image: ImagePlus,
        private var minThreshold: Double,
        private var maxThreshold: Double) : DialogListener {
    override fun dialogItemChanged(dialog: GenericDialog?, awtEvent: AWTEvent?): Boolean {
        if (awtEvent == null || dialog == null)
            return false

        val okButton = this.dialog.buttons[0]
        okButton.isEnabled = false
        val source = awtEvent.source as TextField
        val sliders = dialog.sliders
        val numericFields = dialog.numericFields

        sliders.forEachIndexed { index: Int, _: Any? ->
            if (source == numericFields.elementAt(index)) {
                if (index == 0) {
                    val newMin = Tools.parseDouble(source.text)
                    if (newMin > maxThreshold) {
                        minThreshold = maxThreshold
                        return false
                    }
                    minThreshold = newMin
                } else {
                    val newMax = Tools.parseDouble(source.text)
                    if (minThreshold > newMax) {
                        maxThreshold = minThreshold
                        return false
                    }
                    maxThreshold = newMax
                }
                updateImageThreshold(image, minThreshold = minThreshold.toInt(), maxThreshold = maxThreshold.toInt())
            }
        }
        return true
    }

}

fun updateImageThreshold(image: ImagePlus, minThreshold: Int, maxThreshold: Int) {
    IJ.run("Options...", "iterations=10000 count=1 black")
    Thresholder.setMethod("Default")
    Thresholder.setBackground("Dark")
    IJ.setThreshold(image, minThreshold.toDouble(), maxThreshold.toDouble(), "Black & White")
}
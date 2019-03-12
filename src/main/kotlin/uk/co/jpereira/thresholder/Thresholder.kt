package uk.co.jpereira.thresholder

import ij.IJ
import ij.ImagePlus
import ij.gui.GenericDialog
import ij.io.DirectoryChooser
import ij.plugin.Thresholder
import loci.plugins.BF
import loci.plugins.`in`.ImporterOptions
import org.scijava.command.Command
import org.scijava.plugin.Plugin
import java.io.File


@Plugin(type = Command::class, menuPath = "Plugins>Analyze Images")
open class Thresholder : Command {
    override fun run() {
        val directoryChooser = DirectoryChooser("Select the folder to read images from")
        val directory = File(directoryChooser.directory)

        val channel = ChannelSelectorDialog().open()
        if (channel == Channel.ERROR) {
            print("Canceled while selecting channel")
        }

        directory.listFiles().forEach {
            if (it.extension == "zvi") {
                imageProcess(it, channel)
            }
        }
    }

    private fun imageProcess(image: File, useChannel: Channel) {
        val (cellImage, nucleiImage) = openImage(image, useChannel)

        cellImage.show("original image")

    }

    private fun openImage(imageFile: File, useChannel: Channel): Pair<ImagePlus, ImagePlus> {
        val options = ImporterOptions()
        options.isSplitChannels = true
        options.id = imageFile.absolutePath
        val image = BF.openImagePlus(options)
        val cellImage = image[useChannel.ordinal]
        if (image.size > 2) {
            return Pair(cellImage, image[1])
        }

        return Pair(cellImage, image[Channel.BLUE.ordinal])
    }

    private fun thresholdImage(image: ImagePlus) {
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
        updateImageThreshold(thresholdedImage, 50, 255)

    }

    private fun updateImageThreshold(image: ImagePlus, minThreshold: Int, maxThreshold: Int) {
        IJ.run("Options...", "iterations=10000 count=1 black")
        Thresholder.setMethod("Default")
        Thresholder.setBackground("Dark")
        IJ.setThreshold(image, minThreshold.toDouble(), maxThreshold.toDouble(), "Black & White")
    }
}

enum class Channel {
    RED,
    GREEN,
    BLUE,

    ERROR
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

class ThresholdDialog: GenericDialog("Threshold") {

}
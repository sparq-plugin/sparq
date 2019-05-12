package uk.co.jpereira.sparq.utils

import ij.ImagePlus
import loci.plugins.`in`.ImporterOptions
import uk.co.jpereira.sparq.ImagesToProcess
import uk.co.jpereira.sparq.dialogs.Channel
import uk.co.jpereira.sparq.dialogs.invertToBGR
import java.io.File

interface ImageJOpenImage {
    fun process(imagePath: String): ImagePlus
}

interface BioFormatOpenImage {
    fun process(options: ImporterOptions): Array<ImagePlus>
}

interface ChannelSplitter {
    fun split(image: ImagePlus): Array<ImagePlus>
}

class ImageOpener(private val imageJImageOpener: ImageJOpenImage,
                  private val bioFormatOpenImage: BioFormatOpenImage,
                  private val channelSplitter: ChannelSplitter) {
    fun open(imageFile: File, useChannel: Channel): ImagesToProcess {
        var invertColors = false
        val images: MutableMap<String, Array<ImagePlus>> = mutableMapOf()
        when {
            imageFile.extension == "lif" -> {
                val options = ImporterOptions()
                options.id = imageFile.absolutePath
                options.setOpenAllSeries(true)
                val imgInSeries = bioFormatOpenImage.process(options)
                imgInSeries.forEach {
                    images[it.title] = channelSplitter.split(it)
                }
            }
            imageFile.extension != "tif" -> {
                val options = ImporterOptions()
                options.id = imageFile.absolutePath
                val imgInSeries = bioFormatOpenImage.process(options)[0]
                images[imgInSeries.title] = channelSplitter.split(imgInSeries)

            }
            else -> {
                invertColors = true
                val tifImage = imageJImageOpener.process(imageFile.absolutePath)
                images[tifImage.title] = channelSplitter.split(tifImage)
            }
        }

        val result: ImagesToProcess = mutableMapOf()

        images.forEach {
            val filename = it.key
            val allImages = it.value
            var useChannelArrayPosition = useChannel.ordinal
            var blueChannelArrayPosition = Channel.BLUE.ordinal
            if (invertColors) {
                useChannelArrayPosition = invertToBGR(useChannel)
                blueChannelArrayPosition = invertToBGR(Channel.BLUE)
            }

            val cellImage = allImages[useChannelArrayPosition]
            if (allImages.size == 2) {
                result[filename] = Pair(cellImage, allImages[1])
            } else {
                result[filename] = Pair(cellImage, allImages[blueChannelArrayPosition])
            }
        }
        return result
    }
}
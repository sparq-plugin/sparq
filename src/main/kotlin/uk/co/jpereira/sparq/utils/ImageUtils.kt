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
package uk.co.jpereira.sparq.utils

import ij.IJ
import ij.ImagePlus
import ij.plugin.Thresholder
import loci.plugins.`in`.ImporterOptions
import uk.co.jpereira.sparq.ImagesToProcess
import uk.co.jpereira.sparq.dialogs.Channel
import uk.co.jpereira.sparq.dialogs.invertToBGR
import java.io.File

fun zoomOutImage(image: ImagePlus) {
    IJ.run(image, "Out [-]", "")
    IJ.run(image, "Out [-]", "")
}

fun updateImageThreshold(image: ImagePlus, minThreshold: Int, maxThreshold: Int) {
    IJ.run("Options...", "iterations=10000 count=1 black")
    Thresholder.setMethod("Default")
    Thresholder.setBackground("Dark")
    IJ.setThreshold(image, minThreshold.toDouble(), maxThreshold.toDouble(), "Black & White")
}

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
                val tifImage = imageJImageOpener.process(imageFile.absolutePath)
                images[tifImage.title] = channelSplitter.split(tifImage)
            }
        }

        val result: ImagesToProcess = mutableMapOf()

        images.forEach{
            val filename = it.key
            val allImages = it.value
            var useChannelArrayPosition = useChannel.ordinal
            var blueChannelArrayPosition = Channel.BLUE.ordinal
            if (!allImages[0].stack.isRGB) {
                useChannelArrayPosition = invertToBGR(useChannel)
                blueChannelArrayPosition = invertToBGR(Channel.BLUE)
            }

            val cellImage = allImages[useChannelArrayPosition]
            if (images.size == 2) {
                result[filename] = Pair(cellImage, allImages[1])
            }

            result[filename] = Pair(cellImage, allImages[blueChannelArrayPosition])
        }
        return result
    }
}

/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.utils

import ij.ImagePlus
import ij.ImageStack
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.co.jpereira.sparq.Channel
import uk.co.jpereira.sparq.utils.doubles.*
import java.awt.image.BufferedImage
import java.io.File

fun createRGBImage(name: String): ImagePlus {
    val image = ImagePlus(name, BufferedImage(10, 10, 10))
    val stack = ImageStack(10, 10)
    stack.addSlice("Red", ByteArray(10))
    stack.addSlice("Green", ByteArray(10))
    stack.addSlice("Blue", ByteArray(10))
    image.stack = stack
    return image
}

internal class ImageOpenerTest {
    @Test
    fun `when image as only 2 channels, returns first and second image`() {
        val image = ImagePlus("some-image", BufferedImage(10, 10, 10))
        val redImage = createRGBImage("red-image")
        val blueImage = createRGBImage("blue-image")

        val bioFormatOpenImageStub = BioFormatOpenImageStub(arrayOf(image))
        val channelSplitterStub = ChannelSplitterStub(arrayOf(arrayOf(redImage, blueImage)))
        val imageOpener = ImageOpener(ImageJOpenImageDummy(), bioFormatOpenImageStub, channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.zvi"), Channel.RED)

        assertThat(allImages).hasSize(1)
        val result = allImages["some-image"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(redImage)
        assertThat(result.second).isEqualTo(blueImage)
        assertThat(channelSplitterStub.splitWasCalledWith).isEqualTo(image)
    }

    @Test
    fun `when image is tif and selected channel is RED, returns third and first image`() {
        val image = ImagePlus("some-image", BufferedImage(10, 10, 10))
        val redImage = createRGBImage("red-image")
        val greenImage = createRGBImage("green-image")
        val blueImage = createRGBImage("blue-image")

        val openImageStub = ImageJOpenImageStub(image)
        val channelSplitterStub = ChannelSplitterStub(arrayOf(arrayOf(blueImage, greenImage, redImage)))
        val imageOpener = ImageOpener(openImageStub, BioFormatOpenImageDummy(), channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.tif"), Channel.RED)

        assertThat(allImages).hasSize(1)
        val result = allImages["some-image"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(redImage)
        assertThat(result.second).isEqualTo(blueImage)
        assertThat(channelSplitterStub.splitWasCalledWith).isEqualTo(image)
    }

    @Test
    fun `when image is tif and selected channel is Green, returns second and first image`() {
        val image = ImagePlus("some-image", BufferedImage(10, 10, 10))
        val redImage = createRGBImage("red-image")
        val greenImage = createRGBImage("green-image")
        val blueImage = createRGBImage("blue-image")

        val openImageStub = ImageJOpenImageStub(image)
        val channelSplitterStub = ChannelSplitterStub(arrayOf(arrayOf(blueImage, greenImage, redImage)))
        val imageOpener = ImageOpener(openImageStub, BioFormatOpenImageDummy(), channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.tif"), Channel.GREEN)

        assertThat(allImages).hasSize(1)
        val result = allImages["some-image"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(greenImage)
        assertThat(result.second).isEqualTo(blueImage)
        assertThat(channelSplitterStub.splitWasCalledWith).isEqualTo(image)
    }

    @Test
    fun `when image is zvi and selected channel is RED, returns first and third image`() {
        val image = ImagePlus("some-image", BufferedImage(10, 10, 10))
        val redImage = createRGBImage("red-image")
        val greenImage = createRGBImage("green-image")
        val blueImage = createRGBImage("blue-image")

        val bioFormatOpenImageStub = BioFormatOpenImageStub(arrayOf(image))
        val channelSplitterStub = ChannelSplitterStub(arrayOf(arrayOf(redImage, greenImage, blueImage)))
        val imageOpener = ImageOpener(ImageJOpenImageDummy(), bioFormatOpenImageStub, channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.zvi"), Channel.RED)

        assertThat(allImages).hasSize(1)
        val result = allImages["some-image"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(redImage)
        assertThat(result.second).isEqualTo(blueImage)
    }

    @Test
    fun `when image is zvi and selected channel is GREEN, returns second and third image`() {
        val image = ImagePlus("some-image", BufferedImage(10, 10, 10))
        val redImage = createRGBImage("red-image")
        val greenImage = createRGBImage("green-image")
        val blueImage = createRGBImage("blue-image")

        val bioFormatOpenImageStub = BioFormatOpenImageStub(arrayOf(image))
        val channelSplitterStub = ChannelSplitterStub(arrayOf(arrayOf(redImage, greenImage, blueImage)))
        val imageOpener = ImageOpener(ImageJOpenImageDummy(), bioFormatOpenImageStub, channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.zvi"), Channel.GREEN)

        assertThat(allImages).hasSize(1)
        val result = allImages["some-image"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(greenImage)
        assertThat(result.second).isEqualTo(blueImage)
    }

    @Test
    fun `when image is lif and selected channel is RED, returns first and third image`() {
        val redImage1 = createRGBImage("red-image-1")
        val magentaImage1 = createRGBImage("magenta-image-1")
        val greenImage1 = createRGBImage("green-image-1")
        val blueImage1 = createRGBImage("blue-image-1")
        val redImage2 = createRGBImage("red-image-2")
        val magentaImage2 = createRGBImage("magenta-image-2")
        val greenImage2 = createRGBImage("green-image-2")
        val blueImage2 = createRGBImage("blue-image-2")
        val image1 = ImagePlus("first in series", BufferedImage(10, 10, 10))
        val image2 = ImagePlus("second in series", BufferedImage(10, 10, 10))

        val bioFormatOpenImageStub = BioFormatOpenImageStub(arrayOf(image1, image2))
        val channelSplitterStub = ChannelSplitterStub(arrayOf(
                arrayOf(blueImage1, magentaImage1, greenImage1, redImage1),
                arrayOf(blueImage2, magentaImage2, greenImage2, redImage2)
        ))
        val imageOpener = ImageOpener(ImageJOpenImageDummy(), bioFormatOpenImageStub, channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.lif"), Channel.RED)

        assertThat(allImages).hasSize(2)
        var result = allImages["first in series"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(redImage1)
        assertThat(result.second).isEqualTo(blueImage1)
        result = allImages["second in series"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(redImage2)
        assertThat(result.second).isEqualTo(blueImage2)
        assertThat(bioFormatOpenImageStub.processCalledWith!!.openAllSeries()).isTrue()
    }

    @Test
    fun `when image is lif and selected channel is GREEN, returns second and third image`() {
        val redImage1 = createRGBImage("red-image-1")
        val magentaImage1 = createRGBImage("magenta-image-1")
        val greenImage1 = createRGBImage("green-image-1")
        val blueImage1 = createRGBImage("blue-image-1")
        val redImage2 = createRGBImage("red-image-2")
        val magentaImage2 = createRGBImage("magenta-image-2")
        val greenImage2 = createRGBImage("green-image-2")
        val blueImage2 = createRGBImage("blue-image-2")
        val image1 = ImagePlus("first in series", BufferedImage(10, 10, 10))
        val image2 = ImagePlus("second in series", BufferedImage(10, 10, 10))

        val bioFormatOpenImageStub = BioFormatOpenImageStub(arrayOf(image1, image2))
        val channelSplitterStub = ChannelSplitterStub(arrayOf(
                arrayOf(blueImage1, magentaImage1, greenImage1, redImage1),
                arrayOf(blueImage2, magentaImage2, greenImage2, redImage2)
        ))
        val imageOpener = ImageOpener(ImageJOpenImageDummy(), bioFormatOpenImageStub, channelSplitterStub)
        val allImages = imageOpener.open(File("/some/path.lif"), Channel.GREEN)

        assertThat(allImages).hasSize(2)
        var result = allImages["first in series"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(greenImage1)
        assertThat(result.second).isEqualTo(blueImage1)
        result = allImages["second in series"]
        assertThat(result).isNotNull
        assertThat(result!!.first).isEqualTo(greenImage2)
        assertThat(result.second).isEqualTo(blueImage2)
        assertThat(bioFormatOpenImageStub.processCalledWith!!.openAllSeries()).isTrue()
    }
}
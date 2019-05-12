package uk.co.jpereira.sparq.utils.doubles

import ij.ImagePlus
import loci.plugins.`in`.ImporterOptions
import org.assertj.core.api.Assertions
import uk.co.jpereira.sparq.utils.BioFormatOpenImage
import uk.co.jpereira.sparq.utils.ChannelSplitter
import uk.co.jpereira.sparq.utils.ImageJOpenImage

class ChannelSplitterStub(private var splitStubbed: Array<Array<ImagePlus>>) : ChannelSplitter {
    var splitWasCalledWith: ImagePlus? = null
    override fun split(image: ImagePlus): Array<ImagePlus> {
        splitWasCalledWith = image
        val result = splitStubbed[0]
        splitStubbed = splitStubbed.takeLast(splitStubbed.size - 1).toTypedArray()
        return result
    }
}

class ImageJOpenImageStub(private var processStub: ImagePlus? = null) : ImageJOpenImage {
    override fun process(imagePath: String): ImagePlus {
        return processStub!!
    }
}

class ImageJOpenImageDummy : ImageJOpenImage {
    override fun process(imagePath: String): ImagePlus {
        return Assertions.fail<ImagePlus>("Should not have been called")
    }
}

class BioFormatOpenImageStub(private val processReturns: Array<ImagePlus>) : BioFormatOpenImage {
    var processCalledWith: ImporterOptions? = null
    override fun process(options: ImporterOptions): Array<ImagePlus> {
        processCalledWith = options
        return processReturns
    }
}

class BioFormatOpenImageDummy : BioFormatOpenImage {
    override fun process(options: ImporterOptions): Array<ImagePlus> {
        return Assertions.fail<Array<ImagePlus>>("Should not have been called")
    }
}
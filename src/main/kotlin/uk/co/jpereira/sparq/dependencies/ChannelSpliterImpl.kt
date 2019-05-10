package uk.co.jpereira.sparq.dependencies

import ij.ImagePlus
import uk.co.jpereira.sparq.utils.ChannelSplitter

class ChannelSpliterImpl: ChannelSplitter {
    override fun split(image: ImagePlus): Array<ImagePlus> {
        return ij.plugin.ChannelSplitter.split(image)
    }
}
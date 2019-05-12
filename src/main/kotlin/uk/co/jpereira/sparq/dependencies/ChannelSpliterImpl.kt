/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dependencies

import ij.ImagePlus
import uk.co.jpereira.sparq.utils.ChannelSplitter

class ChannelSpliterImpl: ChannelSplitter {
    override fun split(image: ImagePlus): Array<ImagePlus> {
        return ij.plugin.ChannelSplitter.split(image)
    }
}
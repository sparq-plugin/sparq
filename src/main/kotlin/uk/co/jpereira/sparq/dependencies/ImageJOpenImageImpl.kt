/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dependencies

import ij.IJ
import ij.ImagePlus
import uk.co.jpereira.sparq.utils.ImageJOpenImage

class ImageJOpenImageImpl: ImageJOpenImage {
    override fun process(imagePath: String): ImagePlus {
        return IJ.openImage(imagePath)
    }
}
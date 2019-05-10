package uk.co.jpereira.sparq.dependencies

import ij.IJ
import ij.ImagePlus
import uk.co.jpereira.sparq.utils.ImageJOpenImage

class ImageJOpenImageImpl: ImageJOpenImage {
    override fun process(imagePath: String): ImagePlus {
        return IJ.openImage(imagePath)
    }
}
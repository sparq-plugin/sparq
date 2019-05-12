/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dependencies

import ij.ImagePlus
import loci.plugins.BF
import loci.plugins.`in`.ImporterOptions
import uk.co.jpereira.sparq.utils.BioFormatOpenImage

class BioformatImpl: BioFormatOpenImage {
    override fun process(options: ImporterOptions): Array<ImagePlus> {
        return BF.openImagePlus(options)
    }
}
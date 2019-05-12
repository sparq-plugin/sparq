/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.utils

import ij.IJ
import ij.ImagePlus
import ij.plugin.Thresholder

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

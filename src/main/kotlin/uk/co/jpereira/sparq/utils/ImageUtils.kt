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
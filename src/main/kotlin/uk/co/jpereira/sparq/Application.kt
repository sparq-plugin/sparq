/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import net.imagej.ImageJ

fun main(args: Array<String>) {
    val imj = ImageJ()
    imj.launch(*args)
    imj.command().run(SParQPlugin::class.java, false)
}
/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import java.io.File

enum class Channel(private val channel: Int) {
    ERROR(-1),
    RED(0),
    GREEN(1),
    BLUE(2);

    fun indexOnImage(file: File): Int {
        return when(file.extension) {
            "tif" -> {
                when(this) {
                    BLUE -> 0
                    GREEN -> return 1
                    RED -> return 2
                    else -> -1
                }
            }
            "lif" -> {
                when(this) {
                    BLUE -> 0
                    // Magenta
                    GREEN -> return 2
                    RED -> return 3
                    else -> -1
                }
            }
            else -> this.channel
        }
    }
}
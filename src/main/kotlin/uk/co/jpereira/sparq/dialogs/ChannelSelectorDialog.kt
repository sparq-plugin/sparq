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
package uk.co.jpereira.sparq.dialogs

import ij.gui.GenericDialog

enum class Channel {
    RED,
    GREEN,
    BLUE,

    ERROR
}

fun invertToBGR(channel: Channel): Int {
    return when(channel) {
        Channel.RED -> return 2
        Channel.GREEN -> return 1
        Channel.BLUE -> 0
        else -> -1
    }
}

class ChannelSelectorDialog: GenericDialog("Select channels to use") {
    fun open(): Channel {
        addChoice("Channels:", arrayOf("Red", "Green"), "Red")
        setCancelLabel("Exit Plugin")
        showDialog()
        if (wasCanceled()) {
            return Channel.ERROR
        }
        if (nextChoiceIndex == 0)
            return Channel.RED
        return Channel.GREEN
    }
}
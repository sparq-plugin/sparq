/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dialogs

import ij.gui.GenericDialog
import uk.co.jpereira.sparq.Channel

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
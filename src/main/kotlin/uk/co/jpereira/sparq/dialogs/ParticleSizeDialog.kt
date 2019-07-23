/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dialogs

import ij.gui.GenericDialog
import uk.co.jpereira.sparq.Channel
import java.lang.RuntimeException

class ParticleSizeDialogCanceledException: RuntimeException("Particle Size Dialog was cancelled")

class ParticleSizeDialog: GenericDialog("Particle size") {
    fun open(): Double {
        return getParticleSize("")
    }

    private fun getParticleSize(error: String): Double {
        if (error != "") {
            addMessage(error)
        }
        addStringField("Size of the particles", "0.50")
        setCancelLabel("Exit Plugin")
        showDialog()
        if (wasCanceled()) {
            throw ParticleSizeDialogCanceledException()
        }
        try {
            return nextString.toDouble()
        } catch (err:NumberFormatException) {
            return getParticleSize("Size $nextString need to be a number")
        }
    }
}
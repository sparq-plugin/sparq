/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dialogs

import ij.ImagePlus
import ij.gui.DialogListener
import ij.gui.GenericDialog
import ij.util.Tools
import uk.co.jpereira.sparq.utils.updateImageThreshold
import java.awt.AWTEvent
import java.awt.TextField

class ThresholdDialog(private val image: ImagePlus) : GenericDialog("Threshold") {
    fun show(minThreshold: Double) {
        addSlider("Minimum histogram value", 0.0, 255.0, minThreshold)
        addSlider("Maximum histogram value", 0.0, 255.0, 255.0)
        setCancelLabel("Skip image")
        addDialogListener(
                ThresholdDialogListener(
                        dialog = this,
                        image = image,
                        minThreshold = minThreshold,
                        maxThreshold = 255.0
                ))
        showDialog()
    }
}

class ThresholdDialogListener(
        private val dialog: ThresholdDialog,
        private val image: ImagePlus,
        private var minThreshold: Double,
        private var maxThreshold: Double) : DialogListener {
    override fun dialogItemChanged(dialog: GenericDialog?, awtEvent: AWTEvent?): Boolean {
        if (awtEvent == null || dialog == null)
            return false

        val okButton = this.dialog.buttons[0]
        okButton.isEnabled = false
        val source = awtEvent.source as TextField
        val sliders = dialog.sliders
        val numericFields = dialog.numericFields

        sliders.forEachIndexed { index: Int, _: Any? ->
            if (source == numericFields.elementAt(index)) {
                if (index == 0) {
                    val newMin = Tools.parseDouble(source.text)
                    if (newMin > maxThreshold) {
                        minThreshold = maxThreshold
                        return false
                    }
                    minThreshold = newMin
                } else {
                    val newMax = Tools.parseDouble(source.text)
                    if (minThreshold > newMax) {
                        maxThreshold = minThreshold
                        return false
                    }
                    maxThreshold = newMax
                }
                updateImageThreshold(image, minThreshold = minThreshold.toInt(), maxThreshold = maxThreshold.toInt())
            }
        }
        return true
    }

}
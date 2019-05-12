/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq.dialogs

import ij.gui.GenericDialog
import java.awt.Checkbox

class ExtensionChooserDialog {
    fun open(): List<String> {
        val dialog = GenericDialog("Select files extensions to process")

        dialog.addCheckboxGroup(4, 7, arrayOf(
                "tif", "tiff", "zvi", "lif"
        ), BooleanArray(4))
        dialog.setOKLabel("Select Extensions")
        dialog.setCancelLabel("Exit Plugin")
        dialog.showDialog()

        if (dialog.wasCanceled()) {
            return emptyList()
        }

        return dialog.checkboxes
                .filter { (it as Checkbox).state }
                .map { (it as Checkbox).label }
    }
}
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
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
import ij.WindowManager
import ij.text.TextWindow
import uk.co.jpereira.sparq.printCSVRow
import uk.co.jpereira.sparq.printHeadings
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.PrintWriter

fun saveSummary(filePath: String) {
    val frame = WindowManager.getFrame("Summary")
    IJ.log("saveSummary")
    if (frame == null || frame !is TextWindow)
        return
    val table = frame.textPanel.resultsTable
    val fileOutputStream = FileOutputStream(filePath)
    val buffer = BufferedOutputStream(fileOutputStream)
    val printWriter = PrintWriter(buffer)
    table.printHeadings(printWriter)
    for (i in 0 until table.size())
        table.printCSVRow(i, printWriter)
    printWriter.close()
}

fun removeLastEntryFromSummary() {
    val frame = WindowManager.getFrame("Summary")
    IJ.log("saveSummary")
    if (frame == null || frame !is TextWindow)
        return
    val table = frame.textPanel.resultsTable
    table.deleteRow(table.size() - 1)
}
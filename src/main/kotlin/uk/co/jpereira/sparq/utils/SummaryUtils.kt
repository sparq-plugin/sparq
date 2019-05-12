/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
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
/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import ij.measure.ResultsTable
import java.io.PrintWriter

fun ResultsTable.printHeadings(printWriter: PrintWriter) {
    var line = String()
    headings.forEachIndexed { index: Int, heading: String ->
        line += heading
        if (index != lastColumn)
            line += ","
    }
    if (line.isNotBlank())
        printWriter.println(line)
}

fun ResultsTable.printCSVRow(rowId: Int, printWriter: PrintWriter) {
    var line = ""
    for (i in 0..lastColumn) {
        if (getColumn(i) != null) {
            try {
                var value = getStringValue(i, rowId)
                if (value!!.contains(","))
                    value = "\"" + value + "\""
                line += value
                if (i != lastColumn)
                    line += ","
            } catch (_: IllegalArgumentException) {
                break
            }
        }
    }
    if (line.isNotBlank())
        printWriter.println(line)
}
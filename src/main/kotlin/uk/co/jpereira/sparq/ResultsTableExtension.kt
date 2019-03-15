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
package uk.co.jpereira.sparq

import ij.measure.ResultsTable
import java.io.PrintWriter

fun ResultsTable.printHeadings(printWriter: PrintWriter) {
    var line = String()
    headings.forEachIndexed { index: Int, heading: String ->
        line += heading
        printWriter.print(heading)
        if (index != lastColumn)
            line += ","
    }
    printWriter.println(line)
}

fun ResultsTable.printCSVRow(rowId: Int, printWriter: PrintWriter) {
    var line = ""
    for (i in 0..lastColumn) {
        if (getColumn(i) != null) {
            var value = getStringValue(i, rowId)
            if (value!!.contains(","))
                value = "\"" + value + "\""
            line += value
            if (i != lastColumn)
                line += ","
        }
    }
    printWriter.println(line)
}
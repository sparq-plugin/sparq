/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import ij.measure.ResultsTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

class PrinterWritterSpy(val printlnWasCalledWith: MutableList<String> = mutableListOf()) : PrintWriter(ByteArrayOutputStream()) {
    override fun println(text: String) {
        printlnWasCalledWith.add(text)
    }
}

internal class ResultsTableExtensionTest {
    @Nested
    @DisplayName("#printHeadings")
    open class PrintHeadingsTest {
        @Test
        fun `when table as no headings, it does not print`() {
            val printerSpy = PrinterWritterSpy()
            val subject = ResultsTable()
            subject.printHeadings(printerSpy)
            assertThat(printerSpy.printlnWasCalledWith).isEmpty()
        }

        @Test
        fun `when table as as headings, it does prints them`() {
            val printerSpy = PrinterWritterSpy()
            val subject = ResultsTable()
            subject.incrementCounter()
            subject.addValue("some-label", "some-value")

            subject.incrementCounter()
            subject.addValue("some-other-label", "some-other-value")

            subject.printHeadings(printerSpy)
            assertThat(printerSpy.printlnWasCalledWith).isEqualTo(listOf("some-label,some-other-label"))
        }
    }

    @Nested
    @DisplayName("printCSVRow")
    open class PrintCSVRow {
        @Test
        fun `when not data is in table, it does not print`() {
            val printerSpy = PrinterWritterSpy()
            val subject = ResultsTable()
            subject.printCSVRow(0, printerSpy)
            assertThat(printerSpy.printlnWasCalledWith).isEmpty()
        }

        @Test
        fun `when printing row that does not exist, it does not print`() {
            val printerSpy = PrinterWritterSpy()
            val subject = ResultsTable()

            subject.incrementCounter()
            subject.addValue("some-label", "some-value")

            subject.printCSVRow(1, printerSpy)
            assertThat(printerSpy.printlnWasCalledWith).isEmpty()
        }

        @Test
        fun `when row exists, it prints all columns`() {
            val printerSpy = PrinterWritterSpy()
            val subject = ResultsTable()

            subject.incrementCounter()
            subject.addValue("some-label", "will-not-print")
            subject.addValue("some-other-label", "will-not-print")
            subject.incrementCounter()
            subject.addValue("some-label", "some-value")
            subject.addValue("some-other-label", "some-other-value")

            subject.printCSVRow(1, printerSpy)
            assertThat(printerSpy.printlnWasCalledWith).isEqualTo(listOf("some-value,some-other-value"))
        }
    }
}
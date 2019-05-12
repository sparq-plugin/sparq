/*
 * Copyright 2019 Joao Pereira
 * License can be found in root `LICENSE.md` file
 */
package uk.co.jpereira.sparq

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.io.File

internal class ChannelTest {
    @ParameterizedTest
    @CsvSource(value = ["RED,2", "GREEN,1", "BLUE,0", "ERROR,-1"])
    fun `when image is a tif`(channel: Channel, expected: Int) {
        assertThat(channel.indexOnImage(File("somefile.tif"))).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(value = ["RED,0", "GREEN,1", "BLUE,2", "ERROR,-1"])
    fun `when image is a tiff`(channel: Channel, expected: Int) {
        assertThat(channel.indexOnImage(File("somefile.tiff"))).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(value = ["RED,0", "GREEN,1", "BLUE,2", "ERROR,-1"])
    fun `when image is a zvi`(channel: Channel, expected: Int) {
        assertThat(channel.indexOnImage(File("somefile.zvi"))).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(value = ["RED,3", "GREEN,2", "BLUE,0", "ERROR,-1"])
    fun `when image is a lif`(channel: Channel, expected: Int) {
        assertThat(channel.indexOnImage(File("somefile.lif"))).isEqualTo(expected)
    }
}
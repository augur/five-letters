package com.kilchichakov.fiveletters.service.serde

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource


@ExtendWith(MockKExtension::class)
internal class LetterStatSerDeTest {

    val mapper = ObjectMapper()

    companion object {

        @JvmStatic
        fun source() = listOf(
                arguments(
                        LetterStat(Day(2020, 1, 10), 1),
                        "202001101"
                ),
                arguments(
                        LetterStat(Day(2020, 3, 3), 34),
                        "2020030334"
                ),
                arguments(
                        LetterStat(Day(2020, 11, 13), 2),
                        "202011132"
                ),
                arguments(
                        LetterStat(Day(2020, 10, 5), 255),
                        "20201005255"
                )
        )

    }

    @MethodSource("source")
    @ParameterizedTest
    fun `should serialize examples`(letterStat: LetterStat, expected: String) {
        // When
        val actual = mapper.writeValueAsString(letterStat)

        // Then
        assertThat(actual).isEqualTo("\"" + expected + "\"")
    }

    @MethodSource("source")
    @ParameterizedTest
    fun `should deserialize examples`(expected: LetterStat, serialized: String) {
        // When
        val actual: LetterStat = mapper.readValue(serialized)

        // Then
        assertThat(actual).isEqualTo(expected)
    }
}
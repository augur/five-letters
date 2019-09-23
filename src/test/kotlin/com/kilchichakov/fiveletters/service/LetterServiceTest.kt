package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterPeriodType
import com.kilchichakov.fiveletters.repository.LetterRepository
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.TimeZone


@ExtendWith(MockKExtension::class)
internal class LetterServiceTest {

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)

    @RelaxedMockK
    lateinit var letterRepository: LetterRepository

    @InjectMockKs
    lateinit var service: LetterService

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should send a letter`() {
        // Given
        val login = "poupa"
        val message = "messag"
        val period = LetterPeriodType.THREE_MONTHS
        val offset = -26
        val date = Date()
        val slot = slot<Letter>()

        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["calcOpenDate"](any<LetterPeriodType>(), any<Int>()) } returns date
        every { letterRepository.saveNewLetter(capture(slot)) } just Runs

        // When
        spy.sendLetter(login, message, period, offset)

        // Then
        assertThat(slot.captured._id).isNull()
        assertThat(slot.captured.login).isEqualTo(login)
    }

    @Test
    fun `should get new letters`() {
        // Given
        val login = "poupa"
        val letter = mockk<Letter>()
        every { letterRepository.getNewLetters(any()) } returns listOf(letter)

        // When
        val actual = service.getNewLetters(login)

        // Then
        assertThat(actual).containsExactly(letter)
        verify { letterRepository.getNewLetters(login) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should successfully mark letter as read`() {
        // Given
        val login = "someLogin"
        val id = "someId"
        every { letterRepository.markLetterAsRead(any(), any()) } returns true

        // When
        assertDoesNotThrow { service.markLetterAsRead(login, id) }

        // Then
        verify { letterRepository.markLetterAsRead(login, id) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should throw on unsuccessful mark letter as read`() {
        // Given
        val login = "someLogin"
        val id = "someId"
        every { letterRepository.markLetterAsRead(any(), any()) } returns false

        // When
        assertThrows<DatabaseException> { service.markLetterAsRead(login, id)  }


        // Then
        verify { letterRepository.markLetterAsRead(login, id) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should calc open date correctly`() {
        // Given
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.time = Date.from(instant) // Sat Sep 14 2019 10:51:49 UTC

        mockkStatic(Calendar::class)
        every { Calendar.getInstance(TimeZone.getTimeZone("UTC")) } answers {
            calendar.clone() as Calendar
        }
        val cases = listOf(
                CalcDateCase(LetterPeriodType.WEEK, -180, Date.from(Instant.ofEpochMilli(1569013200000))),
                CalcDateCase(LetterPeriodType.MONTH, -180, Date.from(Instant.ofEpochMilli(1571000400000))),
                CalcDateCase(LetterPeriodType.THREE_MONTHS, -180, Date.from(Instant.ofEpochMilli(1576270800000))),
                CalcDateCase(LetterPeriodType.YEAR, -180, Date.from(Instant.ofEpochMilli(1600030800000))),
                CalcDateCase(LetterPeriodType.THREE_YEARS, -180, Date.from(Instant.ofEpochMilli(1663102800000)))
        )

        // When
        cases.forEach { case ->
            val actual = service.calcOpenDate(case.period, case.offset)
            assertThat(actual).isEqualTo(case.expected)
        }
    }

    data class CalcDateCase(
            val period: LetterPeriodType,
            val offset: Int,
            val expected: Date
    )
}
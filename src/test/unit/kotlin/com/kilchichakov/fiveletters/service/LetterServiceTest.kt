package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.TimePeriod
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.repository.LetterRepository
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCursor
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
import org.junit.jupiter.api.Disabled
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

    @RelaxedMockK
    lateinit var timePeriodService: TimePeriodService

    @InjectMockKs
    lateinit var service: LetterService

    @Test
    fun `should send a letter`() {
        // Given
        val login = "poupa"
        val message = "messag"
        val period = TimePeriod("THREE_MONTHS", 0, 0, 3, 0, true)
        val periodName = "THREE_MONTHS"
        val offset = -26
        val date = Date()
        val slot = slot<Letter>()

        val spy = spyk(service, recordPrivateCalls = true)
        every { timePeriodService.getTimePeriod(any()) } returns period
        every { spy["calcOpenDate"](any<TimePeriod>(), any<Int>(), any<Calendar>()) } returns date
        every { letterRepository.saveNewLetter(capture(slot)) } just Runs

        // When
        spy.sendLetter(login, message, periodName, offset)

        // Then
        assertThat(slot.captured._id).isNull()
        assertThat(slot.captured.login).isEqualTo(login)
        verify {
            timePeriodService.getTimePeriod(periodName)
        }
        confirmVerified(timePeriodService)
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
    fun `should get letters for mail sending`() {
        // Given
        val letter = mockk<Letter>()
        every { letterRepository.getLettersForMailing() } returns listOf(letter)

        // When
        val actual = service.getLettersForMailing()

        // Then
        assertThat(actual).containsExactly(letter)
        verify { letterRepository.getLettersForMailing() }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should get future letter envelops`() {
        // Given
        val login = "loupa"
        val envelop = mockk<SealedLetterEnvelop>()
        every { letterRepository.getFutureLetters(any(), any()) } returns listOf(envelop)

        // When
        val actual = service.getFutureLetters(login)

        // Then
        assertThat(actual).containsExactly(envelop)
        verify { letterRepository.getFutureLetters(login, any()) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should get iterator for all letter envelops`() {
        // Given
        val login = "loupa"
        val iterator = mockk<MongoCursor<SealedLetterEnvelop>>()
        every { letterRepository.iterateLettersDates(any()).iterator() } returns iterator

        // When
        val actual = service.getLettersDatesSequence(login)

        // Then
        verify {
            letterRepository.iterateLettersDates(login)
        }
        confirmVerified(letterRepository)
        assertThat(actual.iterator()).isEqualTo(iterator)
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
    fun `should successfully mark letters as mail sent`() {
        // Given
        val id = "someId"
        every { letterRepository.markLettersAsMailed(any()) } returns true

        // When
        assertDoesNotThrow { service.markLettersAsMailed(listOf(id)) }

        // Then
        verify { letterRepository.markLettersAsMailed(listOf(id)) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should throw on unsuccessful mark letters as mail sent`() {
        // Given
        val id = "someId"
        every { letterRepository.markLettersAsMailed(any()) } returns false

        // When
        assertThrows<DatabaseException> { service.markLettersAsMailed(listOf(id))  }


        // Then
        verify { letterRepository.markLettersAsMailed(listOf(id)) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should calc open date correctly`() {
        // Given
        val weekPeriod = TimePeriod("WEEK", 0, 1, 0, 0, true)
        val monthPeriod = TimePeriod("MONTH", 0, 0, 1, 0, true)
        val month3Period = TimePeriod("THREE_MONTHS", 0, 0, 3, 0, true)
        val yearPeriod = TimePeriod("YEAR", 0, 0, 0, 1, true)
        val year3Period = TimePeriod("THREE_YEARS", 0, 0, 0, 3, true)

        val cases = listOf(
                CalcDateCase(weekPeriod, -180, Date.from(Instant.ofEpochMilli(1569013200000))),
                CalcDateCase(monthPeriod, -180, Date.from(Instant.ofEpochMilli(1571000400000))),
                CalcDateCase(month3Period, -180, Date.from(Instant.ofEpochMilli(1576270800000))),
                CalcDateCase(yearPeriod, -180, Date.from(Instant.ofEpochMilli(1600030800000))),
                CalcDateCase(year3Period, -180, Date.from(Instant.ofEpochMilli(1663102800000)))
        )

        // When
        cases.forEach { case ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.time = Date.from(instant)
            val actual = service.calcOpenDate(case.period, case.offset, calendar)
            assertThat(actual).isEqualTo(case.expected)
        }
    }

    @Test
    fun `should make inbox call`() {
        // Given
        val login = "loupa"
        val request = PageRequest(
                pageNumber = 3,
                pageSize = 20,
                includeRead = true,
                includeMailed = false,
                includeArchived = true,
                sortBy = "poupa"
        )
        val expected = mockk<Page<Letter>>()
        every { letterRepository.inbox(any(), any(), any(), any(), any(), any(), any()) } returns expected

        // When
        val actual = service.getInboxPage(login, request)

        // Then
        assertThat(actual).isEqualTo(expected)
        verify { letterRepository.inbox(login, 40, 20, includeRead = true, includeMailed = false, includeArchived = true, sortBy = "poupa") }
        confirmVerified(letterRepository)
    }

    data class CalcDateCase(
            val period: TimePeriod,
            val offset: Int,
            val expected: Date
    )
}
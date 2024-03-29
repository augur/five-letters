package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.exception.ErrorCode
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.dto.GetLetterStatResponse
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterFreeDateRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.LetterStatDataService
import com.kilchichakov.fiveletters.service.TimePeriodService
import dev.ktobe.toBe
import dev.ktobe.toBeEqual
import dev.ktobe.toContainJust
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Date


@ExtendWith(MockKExtension::class)
internal class LetterControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var letterService: LetterService

    @RelaxedMockK
    lateinit var letterStatDataService: LetterStatDataService

    @RelaxedMockK
    lateinit var timePeriodService: TimePeriodService
    @InjectMockKs
    lateinit var controller: LetterController

    @Test
    fun `should perform send letter call`() {
        // Given
        val message = "some letter"
        val period = "THREE_MONTHS"
        val request = SendLetterRequest(message, period)

        // When
        val actual = controller.send(request)

        // Then
        actual toBeEqual OperationCodeResponse(0)
        verify {
            ControllerUtils.getLogin()
            inputValidationService.validate(request)
            letterService.sendLetter(LOGIN, message, period)
        }
        confirmVerified(inputValidationService, letterService)
    }

    @Test
    fun `should perform send letter with free date call`() {
        // Given
        val message = "some letter"
        val day = Day(2020, 7, 13)
        val request = SendLetterFreeDateRequest(message, day)

        // When
        val actual = controller.sendFreeDate(request)

        // Then
        actual toBeEqual OperationCodeResponse(0)
        verify {
            ControllerUtils.getLogin()
            inputValidationService.validate(request)
            letterService.sendLetter(LOGIN, message, day)
        }
        confirmVerified(inputValidationService, letterService)
    }

    @Test
    fun `should get available time periods`() {
        // Given
        val periods = listOf("WEEK", "MONTH")

        every { timePeriodService.listAllTimePeriods() } returns periods

        // When
        val actual = controller.getTimePeriods()

        // Then
        actual.periods toBeEqual periods
        verify { timePeriodService.listAllTimePeriods() }
        confirmVerified(timePeriodService)
    }

    @Test
    fun `should request inbox page`() {
        // Given
        val request = mockk<PageRequest>()
        val hexString = "5d581a125d9f6680329c6f85"
        val message = "some message"
        val sendDate = Date.from(Instant.ofEpochMilli(100500))
        val openDate = Date.from(Instant.ofEpochMilli(100600))
        val letter = Letter(ObjectId(hexString), LOGIN, message, true, sendDate, openDate)
        val expected = LetterDto(hexString, sendDate, openDate, message, read = true, mailed = false, archived = false)
        val page = Page(listOf(letter), 1,2, 3)

        every { letterService.getInboxPage(any(), any()) } returns page

        // When
        val actual = controller.getInboxPage(request)

        // Then
        actual.elements toContainJust expected
        verify {
            inputValidationService.validate(request)
            letterService.getInboxPage(LOGIN, request)
        }
        confirmVerified(inputValidationService, letterService)
    }

    @Test
    fun `should get future letters`() {
        // Given
        val envelop = mockk<SealedLetterEnvelop>()

        every { letterService.getFutureLetters(LOGIN) } returns listOf(envelop)

        // When
        val actual = controller.getFutureLetters()

        // Then
        actual.letters toContainJust envelop

        verify {
            ControllerUtils.getLogin()
            letterService.getFutureLetters(LOGIN)
        }
        confirmVerified(letterService)
    }

    @Test
    fun `should get letter stats`() {
        // Given
        val expected = mockk<GetLetterStatResponse>()
        every { letterStatDataService.getLetterStats(any()) } returns expected

        // When
        val actual = controller.getLetterStats()

        // Then
        actual toBe expected
        verify {
            letterStatDataService.getLetterStats(LOGIN)
        }
        confirmVerified(letterStatDataService)
    }

    @Test
    fun `should perform mark as read call`() {
        // Given
        val id = "someLetterId"

        // When
        val actual = controller.markAsRead(id)

        // Then
        actual.code toBeEqual ErrorCode.NO_ERROR.numeric
        verify { letterService.markLetterAsRead(LOGIN, id) }
        confirmVerified(letterService)
    }
}
package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.exception.ErrorCode
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.TimePeriodService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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
    lateinit var timePeriodService: TimePeriodService

    @InjectMockKs
    lateinit var controller: LetterController

    @Test
    fun `should perform send letter call`() {
        // Given
        val message = "some letter"
        val period = "THREE_MONTHS"
        val offset = 25
        val request = SendLetterRequest(message, period, offset)

        // When
        val actual = controller.send(request)

        // Then
        assertThat(actual).isEqualTo(OperationCodeResponse(0))
        verify {
            ControllerUtils.getLogin()
            inputValidationService.validate(request)
            letterService.sendLetter(LOGIN, message, period, offset)
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
        assertThat(actual.periods).isEqualTo(periods)
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
        assertThat(actual.elements).containsExactly(expected)
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
        assertThat(actual.letters).containsExactly(envelop)

        verify {
            ControllerUtils.getLogin()
            letterService.getFutureLetters(LOGIN)
        }
        confirmVerified(letterService)
    }

    @Test
    fun `should perform mark as read call`() {
        // Given
        val id = "someLetterId"

        // When
        val actual = controller.markAsRead(id)

        // Then
        assertThat(actual.code).isEqualTo(ErrorCode.NO_ERROR.numeric)
        verify { letterService.markLetterAsRead(LOGIN, id) }
        confirmVerified(letterService)
    }
}
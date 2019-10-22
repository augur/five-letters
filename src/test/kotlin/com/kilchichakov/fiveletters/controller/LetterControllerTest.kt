package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.exception.ErrorCode
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.dto.LetterDto
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.LetterService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Date


@ExtendWith(MockKExtension::class)
internal class LetterControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var letterService: LetterService

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
            letterService.sendLetter(LOGIN, message, period, offset)
        }
        confirmVerified(letterService)
    }

    @Test
    fun `should get new letters`() {
        // Given
        val hexString = "5d581a125d9f6680329c6f85"
        val message = "some message"
        val sendDate = Date.from(Instant.ofEpochMilli(100500))
        val openDate = Date.from(Instant.ofEpochMilli(100600))
        val letter = Letter(ObjectId(hexString), LOGIN, message, false, sendDate, openDate)
        every { letterService.getNewLetters(LOGIN) } returns listOf(letter)

        // When
        val actual = controller.getNewLetters()

        // Then
        assertThat(actual.letters).containsExactly(LetterDto(hexString, sendDate, message))

        verify {
            ControllerUtils.getLogin()
            letterService.getNewLetters(LOGIN)
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
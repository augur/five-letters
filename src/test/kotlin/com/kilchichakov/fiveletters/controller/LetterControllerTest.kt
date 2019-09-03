package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.exception.ErrorCode
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class LetterControllerTest {

    val LOGIN = "someLogin"

    @RelaxedMockK
    lateinit var letterService: LetterService

    @InjectMockKs
    lateinit var controller: LetterController

    @BeforeEach
    fun setUp() {
        mockkObject(ControllerUtils)
        every { ControllerUtils.getLogin() } returns LOGIN
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
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
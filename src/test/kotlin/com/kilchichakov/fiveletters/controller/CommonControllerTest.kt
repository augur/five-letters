package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.service.EmailService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class CommonControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var emailService: EmailService

    @InjectMockKs
    lateinit var controller: CommonController

    private val VERSION = "0.0.1-test"

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        controller = CommonController(VERSION, emailService)
    }

    @Test
    fun `should get version`() {
        // When
        val actual = controller.getVersion()

        // Then
        assertThat(actual).isEqualTo(VERSION)
    }

    @Test
    fun `should confirm email by code`() {
        // Given
        val code = "some code"
        every { emailService.confirmEmailByCode(any()) } just runs

        // When
        val actual = controller.confirmEmail(code)

        // Then
        assertThat(actual).isNotNull()
        verify { emailService.confirmEmailByCode(code) }
        confirmVerified(emailService)
    }
}
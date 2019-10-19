package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.service.PassCodeService
import com.kilchichakov.fiveletters.service.SystemService
import com.mongodb.client.MongoDatabase
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AdminControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var systemService: SystemService

    @RelaxedMockK
    lateinit var passCodeService: PassCodeService

    @InjectMockKs
    lateinit var controller: AdminController

    @Test
    fun `should switch registration`() {
        // Given
        val enabled = true

        // When
        controller.switchRegistration(enabled)

        // Then
        verify {
            ControllerUtils.getLogin()
            systemService.switchRegistration(enabled)
        }
        confirmVerified(ControllerUtils, systemService)
    }

    @Test
    fun `should generate one-time passcode`() {
        // Given
        val seconds = 42L
        val passCode = mockk<OneTimePassCode>()
        every { passCodeService.generateOneTimePassCode(any()) } returns passCode

        // When
        val actual = controller.generateOneTimePassCode(seconds)

        // Then
        assertThat(actual).isEqualTo(passCode)
        verify { passCodeService.generateOneTimePassCode(seconds) }
        confirmVerified(passCodeService)
    }

}
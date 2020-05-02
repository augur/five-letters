package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.service.LetterStatDataService
import com.kilchichakov.fiveletters.service.PassCodeService
import com.kilchichakov.fiveletters.service.SystemService
import com.kilchichakov.fiveletters.service.UserService
import dev.ktobe.toBeEqual
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AdminControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var systemService: SystemService

    @RelaxedMockK
    lateinit var passCodeService: PassCodeService

    @RelaxedMockK
    lateinit var userService: UserService

    @RelaxedMockK
    lateinit var letterStatService: LetterStatDataService

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
        actual toBeEqual passCode
        verify { passCodeService.generateOneTimePassCode(seconds) }
        confirmVerified(passCodeService)
    }

    @Test
    fun `should change user's password`() {
        // Given
        val login = "loupa"
        val pwd = "loupeaux"
        val request = AdminChangePasswordRequest(login, pwd)

        // When
        controller.changeUserPassword(request)

        // Then
        verify {
            inputValidationService.validate(request)
            userService.changeUserPassword(login, pwd)
        }
        confirmVerified(inputValidationService, userService)
    }

    @Test
    fun `should recalculate letter stat data for given user`() {
        // Given
        val login = "loupa"

        // When
        controller.recalculateStats(login)

        // Then
        verify {
            letterStatService.recalculateStatData(login)
        }
        confirmVerified(letterStatService)
    }

    @Test
    fun `should recalculate letter stat data for all users`() {
        // Given
        val users = listOf("loupa", "poupa")
        every { userService.listAllUserLogins() } returns users

        // When
        controller.recalculateStats("@all")

        // Then
        verify {
            userService.listAllUserLogins()
            letterStatService.recalculateStatData("loupa")
            letterStatService.recalculateStatData("poupa")
        }
        confirmVerified(userService, letterStatService)
    }
}
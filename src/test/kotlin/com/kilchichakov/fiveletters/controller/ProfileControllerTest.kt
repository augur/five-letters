package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.exception.ErrorCode
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import com.kilchichakov.fiveletters.service.UserService
import dev.ktobe.toBe
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
internal class ProfileControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var controller: ProfileController

    @Test
    fun `should return whoAmI response`() {
        // Given
        val nick = "loupa"
        val email = "loupa@poupa"
        val userData = UserData(null, LOGIN, nick, email, true)
        every { userService.loadUserData(any()) } returns userData

        // When
        val actual = controller.whoAmI()

        // Then
        actual.nickname toBeEqual nick
        actual.email toBeEqual email
        actual.emailConfirmed toBe true
        verify {
            userService.loadUserData(LOGIN)
        }
        confirmVerified(userService)
    }

    @Test
    fun `should perform update userData`() {
        // Given
        val nick = "poupa"
        val email = "loupa@poupa"
        val request = mockk<UpdateProfileRequest>()
        every { request.nickname } returns nick
        every { request.email } returns email

        // When
        val actual = controller.updateProfile(request)

        // Then
        actual.code toBe ErrorCode.NO_ERROR.numeric
        verify {
            inputValidationService.validate(request)
            userService.updateUserData(LOGIN, email, nick)
        }
        confirmVerified(inputValidationService, userService)
    }
}
package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.service.AuthService
import dev.ktobe.toBeEqual
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class AuthControllerTest : ControllerTestSuite() {

    @MockK
    lateinit var authService: AuthService

    @InjectMockKs
    lateinit var authController: AuthController

    @Test
    fun `should call auth service for auth`() {
        // Given
        val someLogin = "someLogin"
        val somePassword = "somePassword"
        val request = mockk<AuthRequest> {
            every { login } returns someLogin
            every { password } returns somePassword
        }
        val expected = mockk<AuthResponse> {}
        every { authService.authenticate(any(), any()) } returns expected

        // When
        val actual = authController.authenticate(request)

        // Then
        actual toBeEqual expected
        verify {
            inputValidationService.validate(request)
            authService.authenticate(someLogin, somePassword)
        }
        confirmVerified(inputValidationService, authService)
    }
}
package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.service.AuthService
import com.kilchichakov.fiveletters.service.InputValidationService
import io.mockk.clearStaticMockk
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import jdk.internal.util.xml.impl.Input
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder


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
        assertThat(actual).isEqualTo(expected)
        verify {
            inputValidationService.validate(request)
            authService.authenticate(someLogin, somePassword)
        }
        confirmVerified(inputValidationService, authService)
    }

    @Test
    fun `should retrieve login from security context`() {
        // Given
        val someName = "someName"
        mockkStatic(SecurityContextHolder::class)
        val token = mockk<UsernamePasswordAuthenticationToken> {
            every { name } returns someName
        }
        every { SecurityContextHolder.getContext().authentication } returns token

        // When
        val actual = authController.whoAmI()

        // Then
        assertThat(actual).isEqualTo("hello, $someName")
        verify { SecurityContextHolder.getContext().authentication }
        confirmVerified(SecurityContextHolder::class.java)
    }
}
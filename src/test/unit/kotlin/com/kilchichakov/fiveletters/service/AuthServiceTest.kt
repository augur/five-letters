package com.kilchichakov.fiveletters.service

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import java.lang.Exception


@ExtendWith(MockKExtension::class)
internal class AuthServiceTest {

    @RelaxedMockK
    lateinit var authenticationManager: AuthenticationManager

    @RelaxedMockK
    lateinit var jwtService: JwtService

    @InjectMockKs
    lateinit var service: AuthService

    @Test
    fun `should successfully authenticate`() {
        // Given
        val user = "User"
        val password = "pwd"
        val authentication = mockk<Authentication>()
        val userDetals = mockk<UserDetails>()
        val token = "some token"

        val slot = slot<UsernamePasswordAuthenticationToken>()

        every { authenticationManager.authenticate(capture(slot)) } returns authentication
        every { authentication.principal } returns userDetals
        every { jwtService.generateToken(any()) } returns token

        // When
        val actual = service.authenticate(user, password)

        // Then
        assertThat(actual.jwt).isEqualTo(token)
        assertThat(slot.captured.name).isEqualTo(user)
        assertThat(slot.captured.credentials).isEqualTo(password)
        verify {
            authenticationManager.authenticate(any())
            jwtService.generateToken(userDetals)
        }
        confirmVerified(authenticationManager, jwtService)
    }

    @Test
    fun `should throw on DisabledException`() {
        // Given
        every { authenticationManager.authenticate(any()) } throws DisabledException("test")

        // When
        assertThrows<Exception>("USER_DISABLED") { service.authenticate("user", "password") }
    }

    @Test
    fun `should throw on BadCredentialsException`() {
        // Given
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("test")

        // When
        assertThrows<Exception>("INVALID_CREDENTIALS") { service.authenticate("user", "password") }
    }
}
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
import java.util.Date
import org.springframework.beans.factory.annotation.Autowired


@ExtendWith(MockKExtension::class)
internal class AuthServiceTest {

    @RelaxedMockK
    lateinit var authenticationManager: AuthenticationManager

    @RelaxedMockK
    lateinit var jwtService: JwtService

    @RelaxedMockK
    lateinit var refreshTokenService: RefreshTokenService

    @InjectMockKs
    lateinit var service: AuthService

    @Test
    fun `should successfully authenticate`() {
        // Given
        val user = "User"
        val password = "pwd"
        val authentication = mockk<Authentication>()
        val userDetals = mockk<UserDetails>()
        val jwtCode = "some jwt code"
        val jwtDueDate = mockk<Date>()
        val token = mockk<JwtService.EncodedJwt> {
            every { code } returns jwtCode
            every { dueDate } returns jwtDueDate
        }
        val slot = slot<UsernamePasswordAuthenticationToken>()
        val rtCode = "some refresh code"
        val rtDueDate = mockk<Date>()
        val refreshToken = mockk<RefreshTokenService.RefreshToken> {
            every { code } returns rtCode
            every { dueDate } returns rtDueDate
        }

        every { authenticationManager.authenticate(capture(slot)) } returns authentication
        every { authentication.principal } returns userDetals
        every { jwtService.generateToken(any()) } returns token
        every { refreshTokenService.generateRefreshToken(any()) } returns refreshToken

        // When
        val actual = service.authenticate(user, password)

        // Then
        assertThat(actual.login).isEqualTo(user)
        assertThat(actual.jwt).isEqualTo(jwtCode)
        assertThat(actual.jwtDueDate).isEqualTo(jwtDueDate)
        assertThat(actual.refreshToken).isEqualTo(rtCode)
        assertThat(actual.refreshTokenDueDate).isEqualTo(rtDueDate)
        assertThat(slot.captured.name).isEqualTo(user)
        assertThat(slot.captured.credentials).isEqualTo(password)
        verify {
            authenticationManager.authenticate(any())
            jwtService.generateToken(userDetals)
            refreshTokenService.generateRefreshToken(user)
        }
        confirmVerified(authenticationManager, jwtService, refreshTokenService)
    }

    @Test
    fun `should throw on DisabledException`() {
        // Given
        every { authenticationManager.authenticate(any()) } throws DisabledException("test")

        // When
        assertThrows<DisabledException> { service.authenticate("user", "password") }
    }

    @Test
    fun `should throw on BadCredentialsException`() {
        // Given
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("test")

        // When
        assertThrows<BadCredentialsException> { service.authenticate("user", "password") }
    }
}
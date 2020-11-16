package com.kilchichakov.fiveletters.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.FoundEmailUnconfirmed
import com.kilchichakov.fiveletters.model.FoundOk
import com.kilchichakov.fiveletters.model.NotFound
import com.kilchichakov.fiveletters.model.dto.AuthEmailNotFound
import com.kilchichakov.fiveletters.model.dto.AuthEmailUnconfirmed
import com.kilchichakov.fiveletters.model.dto.AuthSuccess
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import dev.ktobe.CollectionKeyword.empty
import dev.ktobe.toBe
import dev.ktobe.toBeEqual
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import java.util.Date
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails

@ExtendWith(MockKExtension::class)
internal class AuthGoogleServiceTest {

    @RelaxedMockK
    lateinit var authDataRepository: AuthDataRepository

    @RelaxedMockK
    lateinit var jwtService: JwtService

    @RelaxedMockK
    lateinit var verifier: GoogleIdTokenVerifier

    @RelaxedMockK
    lateinit var jobService: JobService

    @RelaxedMockK
    lateinit var refreshTokenService: RefreshTokenService

    @InjectMockKs
    lateinit var service: AuthGoogleService

    @Test
    fun `should throw when verification not passed`() {
        // Given
        val idTokenString = "some string that is longer than 20 characters"
        every { verifier.verify(any<String>()) } returns null

        // When
        assertThrows<BadCredentialsException> {
            service.authenticate(idTokenString)
        }

        // Then
        verify { verifier.verify(idTokenString) }
        confirmVerified(authDataRepository, jwtService, verifier, jobService)
    }

    @Test
    fun `should authenticate successfully`() {
        // Given
        val email = "some@email"
        val idToken = mockk<GoogleIdToken> {
            every { payload.email } returns email
        }
        val idTokenString = "some string that is longer than 20 characters"
        every { verifier.verify(any<String>()) } returns idToken
        val login = "some login"
        val password = "some password"
        val authData = AuthData(null, login, password, false)
        val searchResult = FoundOk(authData)
        every { authDataRepository.findAuthDataByEmail(any()) } returns searchResult

        val jwtCode = "some jwt code"
        val jwtDueDate = mockk<Date>()
        val refreshTokenCode = "some refresh token coode"
        val refreshTokenDueDate = mockk<Date>()
        val jwt = mockk<JwtService.EncodedJwt> {
            every { code } returns jwtCode
            every { dueDate } returns jwtDueDate
        }
        val refreshToken = mockk<RefreshTokenService.RefreshToken> {
            every { code } returns refreshTokenCode
            every { dueDate } returns refreshTokenDueDate
        }

        every { jwtService.generateToken(any()) } returns jwt
        every { refreshTokenService.generateRefreshToken(any()) } returns refreshToken

        // When
        val actual = service.authenticate(idTokenString)

        // Then
        assertThat(actual).isInstanceOf(AuthSuccess::class.java)
        actual as AuthSuccess
        actual.auth.login toBeEqual login
        actual.auth.jwt toBeEqual jwtCode
        actual.auth.jwtDueDate toBeEqual jwtDueDate
        actual.auth.refreshToken toBeEqual refreshTokenCode
        actual.auth.refreshTokenDueDate toBeEqual refreshTokenDueDate
        val userSlot = slot<UserDetails>()
        verifyOrder {
            verifier.verify(idTokenString)
            authDataRepository.findAuthDataByEmail(email)
            jwtService.generateToken(capture(userSlot))
            refreshTokenService.generateRefreshToken(login)
        }
        userSlot.captured.username toBe login
        userSlot.captured.authorities toBe empty
        confirmVerified(authDataRepository, jwtService, verifier, jobService, refreshTokenService)
    }

    @Test
    fun `should authenticate and find unconfirmed email`() {
        // Given
        val email = "some@email"
        val idToken = mockk<GoogleIdToken> {
            every { payload.email } returns email
        }
        val idTokenString = "some string that is longer than 20 characters"
        every { verifier.verify(any<String>()) } returns idToken
        val login = "some login"
        val password = "some password"
        val authData = AuthData(null, login, password, false)
        val searchResult = FoundEmailUnconfirmed(authData)
        every { authDataRepository.findAuthDataByEmail(any()) } returns searchResult

        // When
        val actual = service.authenticate(idTokenString)

        // Then
        assertThat(actual).isInstanceOf(AuthEmailUnconfirmed::class.java)
        actual as AuthEmailUnconfirmed
        actual.email toBeEqual email
        verifyOrder {
            verifier.verify(idTokenString)
            authDataRepository.findAuthDataByEmail(email)
            jobService.scheduleEmailConfirmation(login)
        }
        confirmVerified(authDataRepository, jwtService, verifier, jobService)
    }

    @Test
    fun `should authenticate and not to find email`() {
        // Given
        val email = "some@email"
        val idToken = mockk<GoogleIdToken> {
            every { payload.email } returns email
        }
        val idTokenString = "some string that is longer than 20 characters"
        every { verifier.verify(any<String>()) } returns idToken
        val searchResult = NotFound
        every { authDataRepository.findAuthDataByEmail(any()) } returns searchResult

        // When
        val actual = service.authenticate(idTokenString)

        // Then
        assertThat(actual).isInstanceOf(AuthEmailNotFound::class.java)
        actual as AuthEmailNotFound
        actual.email toBeEqual email
        verifyOrder {
            verifier.verify(idTokenString)
            authDataRepository.findAuthDataByEmail(email)
        }
        confirmVerified(authDataRepository, jwtService, verifier, jobService)
    }

}
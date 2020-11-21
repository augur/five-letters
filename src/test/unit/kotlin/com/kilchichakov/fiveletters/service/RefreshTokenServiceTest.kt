package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import dev.ktobe.toBe
import dev.ktobe.toBeEqual
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verifyOrder
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class RefreshTokenServiceTest {

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309000)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))

    private val ttlSeconds = 10L

    @RelaxedMockK
    lateinit var authDataRepository: AuthDataRepository

    lateinit var service: RefreshTokenService

    @BeforeEach
    fun setUp() {
        service = RefreshTokenService(ttlSeconds, authDataRepository, clock)
    }

    @Test
    fun `should generate refresh token`() {
        // Given
        val login = "some user"
        every { authDataRepository.setRefreshToken(any(), any(), any()) } returns true

        // When
        val actual = service.generateRefreshToken(login)

        // Then
        actual.dueDate toBeEqual Date.from(instant.plusSeconds(ttlSeconds))
        verifyOrder { authDataRepository.setRefreshToken(login, actual.code, actual.dueDate) }
        confirmVerified(authDataRepository)
    }

    @Test
    fun `should throw when refresh token wasn't stored`() {
        // Given
        val login = "some user"
        every { authDataRepository.setRefreshToken(any(), any(), any()) } returns false

        // When, Then
        assertThatCode {
            service.generateRefreshToken(login)
        }.isExactlyInstanceOf(DatabaseException::class.java)
    }

    @Test
    fun `should validate refresh token successfully`() {
        // Given
        val code = "some code"
        val authData = mockk<AuthData> {
            every { refreshTokenDueDate } returns Date.from(instant)
            every { refreshToken } returns code
        }

        // When
        val actual = service.validateRefreshToken(code, authData)

        // Then
        actual toBe true
    }

    @Test
    fun `should not validate refresh token - expired`() {
        // Given
        val code = "some code"
        val authData = mockk<AuthData> {
            every { refreshTokenDueDate } returns Date.from(instant.minusMillis(1))
            every { refreshToken } returns code
        }

        // When
        val actual = service.validateRefreshToken(code, authData)

        // Then
        actual toBe false
    }

    @Test
    fun `should not validate refresh token - wrong code`() {
        // Given
        val code = "some code"
        val authData = mockk<AuthData> {
            every { refreshTokenDueDate } returns Date.from(instant)
            every { refreshToken } returns code + code
        }

        // When
        val actual = service.validateRefreshToken(code, authData)

        // Then
        actual toBe false
    }
}

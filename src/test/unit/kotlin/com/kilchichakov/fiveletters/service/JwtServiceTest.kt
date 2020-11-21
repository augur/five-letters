package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.AuthData
import dev.ktobe.toBeEqual
import dev.ktobe.toContainJust
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class JwtServiceTest {

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(12568458309000)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))

    lateinit var service: JwtService

    @BeforeEach
    fun setUp() {
        service = JwtService("secret", "issuer", 42, clock)
    }

    @Test
    fun `should generate and then validate token`() {
        // Given
        val user = "loupeaux"
        val authorities = mutableListOf(SimpleGrantedAuthority("loupa"),
                SimpleGrantedAuthority("poupa"))

        val userDetails = mockk<UserDetails>()
        every { userDetails.username } returns user
        every { userDetails.authorities } returns authorities

        // When
        val token = service.generateToken(userDetails)
        val validated = service.validateToken(token.code)

        // Then
        verify {
            userDetails.username
            userDetails.authorities
        }

        token.dueDate toBeEqual Date.from(instant.plusMillis(42000))
        assertThat(validated.username).isEqualTo(user)
        assertThat(validated.roles).containsExactlyInAnyOrder("loupa", "poupa")
        assertThat(validated.expiresAt).isEqualTo(Date.from(instant.plusMillis(42000)))
    }

    @Test
    fun `should generate token from auth data`() {
        // Given
        val user = "loupeaux"

        val authData = mockk<AuthData>()
        every { authData.login } returns user
        every { authData.admin } returns true

        // When
        val token = service.generateToken(authData)
        val validated = service.validateToken(token.code)

        // Then
        token.dueDate toBeEqual Date.from(instant.plusMillis(42000))
        assertThat(validated.username).isEqualTo(user)
        validated.roles toContainJust "ROLE_ADMIN"
        assertThat(validated.expiresAt).isEqualTo(Date.from(instant.plusMillis(42000)))
    }

    @Test
    fun `should return null user on failure`() {
        // Given
        val token = "some.random.invalidtoken"

        // When
        val actual = service.validateToken(token)

        // Then
        assertThat(actual.username).isNull()
        assertThat(actual.roles).isEmpty()
    }
}
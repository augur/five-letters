package com.kilchichakov.fiveletters.service

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Calendar
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class JwtServiceTest {

    @RelaxedMockK
    lateinit var calendar: Calendar

    lateinit var service: JwtService

    @BeforeEach
    fun setUp() {
        service = JwtService("secret", "issuer", 42)
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns calendar
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should generate and then validate token`() {
        // Given
        val user = "loupeaux"
        val authorities = listOf(SimpleGrantedAuthority("loupa"),
                SimpleGrantedAuthority("poupa"))

        val userDetails = mockk<UserDetails>()
        every { userDetails.username } returns user
        every { userDetails.authorities } returns authorities
        every { calendar.time } returns Date()

        // When
        val token = service.generateToken(userDetails)
        val validated = service.validateToken(token)

        // Then
        verify {
            Calendar.getInstance()
            userDetails.username
            userDetails.authorities
            calendar.time
        }

        assertThat(validated.username).isEqualTo(user)
        assertThat(validated.roles).containsExactlyInAnyOrder("loupa", "poupa")
    }

    @Test()
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
package com.kilchichakov.fiveletters.filter

import com.kilchichakov.fiveletters.invokePrivate
import com.kilchichakov.fiveletters.service.JwtService
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockKExtension::class)
internal class JwtRequestFilterTest {

    @RelaxedMockK
    lateinit var jwtService: JwtService

    @InjectMockKs
    lateinit var filter: JwtRequestFilter

    @BeforeEach
    fun setUp() {
        mockkStatic(SecurityContextHolder::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should authorize by token`() {
        // Given
        val request = mockk<HttpServletRequest>(relaxed = true)
        val response = mockk<HttpServletResponse>(relaxed = true)
        val chain = mockk<FilterChain>(relaxed = true)
        val token = "Bearer loupeaux"
        val decodedJwt = JwtService.DecodedJwt("loupa", listOf("poupa"))
        val slot = slot<UsernamePasswordAuthenticationToken>()

        every { request.getHeader(any()) } returns token
        every { jwtService.validateToken(any()) } returns decodedJwt
        every { SecurityContextHolder.getContext().authentication } returns null
        every { SecurityContextHolder.getContext().setAuthentication(capture(slot)) } just Runs

        // When
        filter.invokePrivate("doFilterInternal", request, response, chain)

        // Then
        assertThat(slot.captured.name).isEqualTo("loupa")
        assertThat(slot.captured.authorities).containsExactly(SimpleGrantedAuthority("poupa"))
        assertThat(slot.captured.credentials).isNull()
        verify {
            request.getHeader("Authorization")
            jwtService.validateToken("loupeaux")
            chain.doFilter(request, response)
        }
        confirmVerified(jwtService, chain, response)
    }

    @Test
    fun `should not authorize if token missing`() {
        // Given
        val request = mockk<HttpServletRequest>(relaxed = true)
        val response = mockk<HttpServletResponse>(relaxed = true)
        val chain = mockk<FilterChain>(relaxed = true)

        // When
        filter.invokePrivate("doFilterInternal", request, response, chain)

        verify {
            request.getHeader("Authorization")
            chain.doFilter(request, response)
        }
        verify(exactly = 0) { jwtService.validateToken(any()) }
        confirmVerified(jwtService, chain, response)
    }

    @Test
    fun `should not replace authorization`() {
        // Given
        val request = mockk<HttpServletRequest>(relaxed = true)
        val response = mockk<HttpServletResponse>(relaxed = true)
        val chain = mockk<FilterChain>(relaxed = true)
        val token = "Bearer loupeaux"
        val decodedJwt = JwtService.DecodedJwt("loupa", listOf("poupa"))
        val context = mockk<SecurityContext>()
        val authentication = mockk<Authentication>()

        every { request.getHeader(any()) } returns token
        every { jwtService.validateToken(any()) } returns decodedJwt
        every { SecurityContextHolder.getContext() } returns context
        every { context.authentication } returns authentication

        // When
        filter.invokePrivate("doFilterInternal", request, response, chain)

        // Then
        verify {
            request.getHeader("Authorization")
            jwtService.validateToken("loupeaux")
            chain.doFilter(request, response)
        }
        verify(exactly = 0) {
            context.setAuthentication(any())
        }
        confirmVerified(jwtService, chain, response)
    }
}
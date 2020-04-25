package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.exception.ErrorCode
import dev.ktobe.toBe
import dev.ktobe.toBeEqual
import io.mockk.clearStaticMockk
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder


@ExtendWith(MockKExtension::class)
internal class ControllerUtilsTest {

    private val LOGIN = "someName"

    private val utils = ControllerUtils

    @BeforeEach
    fun setUp() {
        // Given
        val someName = LOGIN
        val token = mockk<UsernamePasswordAuthenticationToken> {
            every { name } returns someName
        }
        val context = mockk<SecurityContext>()
        every { context.authentication } returns token
        SecurityContextHolder.setContext(context)
    }

    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should get login from security context`() {
        // When
        val actual = utils.getLogin()

        // Then
        actual toBe LOGIN
    }

    @Test
    fun `should successfully process while authorized`() {
        // Given
        val block: (String?) -> Unit = mockk(relaxed = true)

        // When
        val actual = utils.processAndRespondCode(block = block)

        // Then
        actual.code toBeEqual ErrorCode.NO_ERROR.numeric
        verify { block(LOGIN) }
        confirmVerified(block)
    }

    @Test
    fun `should successfully process while unauthorized`() {
        // Given
        val block: (String?) -> Unit = mockk(relaxed = true)

        // When
        val actual = utils.processAndRespondCode(false, block)

        // Then
        actual.code toBeEqual ErrorCode.NO_ERROR.numeric
        verify { block(null) }
        confirmVerified(block)
    }

    @Test
    fun `should process while having backend exception`() {
        // Given
        val block: (String?) -> Unit = mockk(relaxed = true)
        every { block(any()) } throws DatabaseException("message")

        // When
        val actual = utils.processAndRespondCode(false, block)

        // Then
        actual.code toBeEqual ErrorCode.DB.numeric
        verify { block(null) }
        confirmVerified(block)
    }

    @Test
    fun `should process while having generic exception`() {
        // Given
        val block: (String?) -> Unit = mockk(relaxed = true)
        every { block(any()) } throws ClassCastException("message")

        // When
        val actual = utils.processAndRespondCode(false, block)

        // Then
        actual.code toBeEqual ErrorCode.GENERIC_ERROR.numeric
        verify { block(null) }
        confirmVerified(block)
    }
}
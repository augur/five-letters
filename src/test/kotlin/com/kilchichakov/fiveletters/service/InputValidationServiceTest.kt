package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.invokePrivate
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.service.InputValidationService.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class InputValidationServiceTest {

    @InjectMockKs
    lateinit var service: InputValidationService

    @Test
    fun `should validate send letter request`() {
        // Given
        val message = "some message!!3"
        val period = "DAY"
        val offset = -180
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkMessage"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPeriod"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkTimezoneOffset"](any<ValidationResult>(), any<Int>()) } returns 0

        // When
        spy.validate(SendLetterRequest(message, period, offset))

        // Then
        verify { spy["checkMessage"](any<ValidationResult>(), message) }
        verify { spy["checkPeriod"](any<ValidationResult>(), period) }
        verify { spy["checkTimezoneOffset"](any<ValidationResult>(), offset) }
    }

    @Test
    fun `should validate auth request`() {
        // Given
        val login = "loupa"
        val pwd = "poupa"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkLogin"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassword"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(AuthRequest(login, pwd))

        // Then
        verify { spy["checkLogin"](any<ValidationResult>(), login) }
        verify { spy["checkPassword"](any<ValidationResult>(), pwd) }
    }

    @Test
    fun `should validate admin change password request`() {
        // Given
        val login = "loupa"
        val pwd = "poupa"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkLogin"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassword"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(AdminChangePasswordRequest(login, pwd))

        // Then
        verify { spy["checkLogin"](any<ValidationResult>(), login) }
        verify { spy["checkPassword"](any<ValidationResult>(), pwd) }
    }

    @Test
    fun `should validate register request`() {
        // Given
        val login = "loupa"
        val pwd = "poupa"
        val passCode = "xxx-yyy-zzz"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkLogin"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassword"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassCode"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(RegisterRequest(login, pwd, false, passCode))

        // Then
        verify { spy["checkLogin"](any<ValidationResult>(), login) }
        verify { spy["checkPassword"](any<ValidationResult>(), pwd) }
        verify { spy["checkPassCode"](any<ValidationResult>(), passCode) }
    }


    @Test
    fun `should check login`() {
        // Given
        val testCases = listOf(
                StringCase("Abc123", true),
                StringCase("", false),
                StringCase("Кириллица", false),
                StringCase("spac es", false),
                StringCase("abcabcabcabcabca", false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkLogin", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check password`() {
        // Given
        val testCases = listOf(
                StringCase("Ват вер123", true),
                StringCase("", false),
                StringCase("1234512345123451234512345123451", false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkPassword", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check pass-code`() {
        // Given
        val testCases = listOf(
                StringCase("xxx-yyy-zzz", true),
                StringCase("x".repeat(101), false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkPassCode", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check message`() {
        // Given
        val testCases = listOf(
                StringCase("Blah Blah Blah", true),
                StringCase("                tiny                                ", false),
                StringCase("X".repeat(2001), false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkMessage", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check period`() {
        // Given
        val testCases = listOf(
                StringCase("THREE_DAYS", true),
                StringCase("", false),
                StringCase("X".repeat(21), false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkPeriod", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check time-zone offset`() {
        // Given
        val testCases = listOf(
                IntCase(0,true),
                IntCase(1000, false),
                IntCase(-1000, false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkTimezoneOffset", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    data class StringCase(val value: String, val valid: Boolean)

    data class IntCase(val value: Int, val valid: Boolean)
}
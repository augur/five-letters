package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.invokePrivate
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import com.kilchichakov.fiveletters.service.InputValidationService.*
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.runs
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
        val email = "email"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkLogin"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassword"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassCode"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkEmail"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(RegisterRequest(login, pwd, false, passCode, email))

        // Then
        verify {
            spy["checkLogin"](any<ValidationResult>(), login)
            spy["checkPassword"](any<ValidationResult>(), pwd)
            spy["checkPassCode"](any<ValidationResult>(), passCode)
            spy["checkEmail"](any<ValidationResult>(), email)
        }
    }

    @Test
    fun `should validate page request`() {
        // Given
        val pageNumber = 11
        val pageSize = 239
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkPageNumber"](any<ValidationResult>(), any<Int>()) } returns 0
        every { spy["checkPageSize"](any<ValidationResult>(), any<Int>()) } returns 0

        // When
        spy.validate(PageRequest(pageNumber, pageSize, includeRead = true, includeMailed = false, includeArchived = true))

        // Then
        verify { spy["checkPageNumber"](any<ValidationResult>(), pageNumber) }
        verify { spy["checkPageSize"](any<ValidationResult>(), pageSize) }
    }

    @Test
    fun `should validate update profile request`() {
        // Given
        val email = "email"
        val nickname = "nickname"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkEmail"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkNickname"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(UpdateProfileRequest(email, nickname))

        // Then
        verify { spy["checkEmail"](any<ValidationResult>(), email) }
        verify { spy["checkNickname"](any<ValidationResult>(), nickname) }
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

    @Test
    fun `should check page number`() {
        // Given
        val testCases = listOf(
                IntCase(-5, false),
                IntCase(0, false),
                IntCase(1, true),
                IntCase(5, true)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkPageNumber", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check page size`() {
        // Given
        val testCases = listOf(
                IntCase(-5, false),
                IntCase(0, false),
                IntCase(1, true),
                IntCase(100, true),
                IntCase(150, false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkPageSize", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check email`() {
        // Given
        val testCases = listOf(
                StringCase("", false),
                StringCase("${"X".repeat(100)}@mail.com", false),
                StringCase("blalba.net", false),
                StringCase("normal@email.com", true)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkEmail", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    @Test
    fun `should check nickname`() {
        // Given
        val testCases = listOf(
                StringCase("Loupa", true),
                StringCase("", false),
                StringCase("Poupa".repeat(7), false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkNickname", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    data class StringCase(val value: String, val valid: Boolean)

    data class IntCase(val value: Int, val valid: Boolean)
}
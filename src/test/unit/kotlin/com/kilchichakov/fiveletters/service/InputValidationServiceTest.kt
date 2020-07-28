package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.invokePrivate
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterFreeDateRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import com.kilchichakov.fiveletters.service.InputValidationService.*
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
internal class InputValidationServiceTest {

    @InjectMockKs
    lateinit var service: InputValidationService

    @Test
    fun `should validate send letter request`() {
        // Given
        val message = "some message!!3"
        val period = "DAY"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkMessage"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPeriod"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(SendLetterRequest(message, period))

        // Then
        verify { spy["checkMessage"](any<ValidationResult>(), message) }
        verify { spy["checkPeriod"](any<ValidationResult>(), period) }
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
        val timezone = "Singapore"
        every { spy["checkLogin"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassword"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkPassCode"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkEmail"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkTimeZone"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(RegisterRequest(login, pwd, false, passCode, email, timezone))

        // Then
        verify {
            spy["checkLogin"](any<ValidationResult>(), login)
            spy["checkPassword"](any<ValidationResult>(), pwd)
            spy["checkPassCode"](any<ValidationResult>(), passCode)
            spy["checkEmail"](any<ValidationResult>(), email)
            spy["checkTimeZone"](any<ValidationResult>(), timezone)
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
        every { spy["checkInboxSortBy"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(PageRequest(pageNumber, pageSize, includeRead = true, includeMailed = false, includeArchived = true, sortBy = "poupa"))

        // Then
        verify { spy["checkPageNumber"](any<ValidationResult>(), pageNumber) }
        verify { spy["checkPageSize"](any<ValidationResult>(), pageSize) }
        verify { spy["checkInboxSortBy"](any<ValidationResult>(), "poupa") }
    }

    @Test
    fun `should validate update profile request`() {
        // Given
        val email = "email"
        val nickname = "nickname"
        val timezone = "Singapore"
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkEmail"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkNickname"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkTimeZone"](any<ValidationResult>(), any<String>()) } returns 0

        // When
        spy.validate(UpdateProfileRequest(email, nickname, timezone))

        // Then
        verify {
            spy["checkEmail"](any<ValidationResult>(), email)
            spy["checkNickname"](any<ValidationResult>(), nickname)
            spy["checkTimeZone"](any<ValidationResult>(), timezone)
        }
    }

    @Test
    fun `should validate send letter free date request`() {
        // Given
        val message = "some message"
        val day = Day(3001, 10, 25)
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["checkMessage"](any<ValidationResult>(), any<String>()) } returns 0
        every { spy["checkOpenDate"](any<ValidationResult>(), any<Day>()) } returns 0

        // When
        spy.validate(SendLetterFreeDateRequest(message, day))

        // Then
        verify {
            spy["checkMessage"](any<ValidationResult>(), message)
            spy["checkOpenDate"](any<ValidationResult>(), day)
        }
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
    fun `should check inbox sortBy`() {
        // Given
        val testCases = listOf(
                TestCase("", false),
                TestCase(null, true),
                TestCase("sendDate", true),
                TestCase("openDate", true),
                TestCase("login", false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkInboxSortBy", vResult, it.value)
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
    fun `should check timeZone`() {
        // Given
        val testCases = listOf(
                StringCase("Singapore", true),
                StringCase("US/Michigan", true),
                StringCase("Indian/Christmas", true),
                StringCase("Stygian", false)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkTimeZone", vResult, it.value)
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

    @Test
    fun `should check open date`() {
        // Given
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        val yesterday = now.minusDays(1)
        val tomorrow = now.plusDays(1)

        val dayNow = Day(now.year.toShort(), now.monthValue.toByte(), now.dayOfMonth.toByte())
        val dayYesterday = Day(yesterday.year.toShort(), yesterday.monthValue.toByte(), yesterday.dayOfMonth.toByte())
        val dayTomorrow = Day(tomorrow.year.toShort(), tomorrow.monthValue.toByte(), tomorrow.dayOfMonth.toByte())

        val testCases = listOf(
                TestCase(dayNow, false),
                TestCase(dayYesterday, false),
                TestCase(dayTomorrow, true)
        )

        // Then
        testCases.forEach {
            val vResult = ValidationResult(ArrayList())
            service.invokePrivate("checkOpenDate", vResult, it.value)
            assertThat(vResult.errors.isEmpty()).isEqualTo(it.valid)
        }
    }

    data class TestCase<T>(val value: T?, val valid: Boolean)

    data class StringCase(val value: String, val valid: Boolean)

    data class IntCase(val value: Int, val valid: Boolean)
}
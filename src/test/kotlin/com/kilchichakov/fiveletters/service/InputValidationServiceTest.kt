package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.invokePrivate
import com.kilchichakov.fiveletters.model.dto.AuthRequest
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
    fun `should check login`() {
        // Given
        val testCases = listOf(
                StringCase("Abc", true),
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

    data class StringCase(val value: String, val valid: Boolean)
}
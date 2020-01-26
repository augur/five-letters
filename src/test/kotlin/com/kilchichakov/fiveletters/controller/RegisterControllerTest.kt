package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.service.UserService
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class RegisterControllerTest : ControllerTestSuite() {

    @RelaxedMockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var controller: RegisterController

    @Test
    fun `should register`() {
        // Given
        val login = "lg"
        val pwd = "pw"
        val acceptLicence = true
        val passCode = "XXX-YYY-ZZZ"
        val email = "email"
        val request = RegisterRequest(login, pwd, acceptLicence, passCode, email)

        // When
        val actual = controller.register(request)

        // Then
        assertThat(actual).isEqualTo(OperationCodeResponse(0))
        verify(exactly = 0) { ControllerUtils.getLogin() }
        verify {
            inputValidationService.validate(request)
            userService.registerNewUser(login, pwd, acceptLicence, passCode, email)
        }
        confirmVerified(inputValidationService, userService)
    }
}
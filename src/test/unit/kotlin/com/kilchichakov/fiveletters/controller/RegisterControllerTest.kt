package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.ControllerTestSuite
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.service.UserService
import dev.ktobe.toBeEqual
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
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
        val timezone = "Singapore"
        val request = RegisterRequest(login, pwd, acceptLicence, passCode, email, timezone)

        // When
        val actual = controller.register(request)

        // Then
        actual toBeEqual OperationCodeResponse(0)
        verify(exactly = 0) { ControllerUtils.getLogin() }
        verify {
            inputValidationService.validate(request)
            userService.registerNewUser(login, pwd, acceptLicence, passCode, email, timezone)
        }
        confirmVerified(inputValidationService, userService)
    }
}
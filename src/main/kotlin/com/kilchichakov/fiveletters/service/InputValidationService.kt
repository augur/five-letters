package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import org.springframework.stereotype.Service

@Service
class InputValidationService {

    fun validate(input: Any) {
        when(input) {
            is SendLetterRequest -> doValidate(input)
            is AuthRequest -> doValidate(input)
            is RegisterRequest -> doValidate(input)
            is AdminChangePasswordRequest -> doValidate(input)
            else -> LOG.warn { "no validation rules for $input" }
        }
    }

    private fun doValidate(letterRequest: SendLetterRequest) {
        validation(letterRequest) {
            checkMessage(letterRequest.message)
            checkPeriod(letterRequest.period)
            checkTimezoneOffset(letterRequest.timezoneOffset)
        }
    }

    private fun doValidate(authRequest: AuthRequest) {
        validation(authRequest) {
            checkLogin(authRequest.login)
            checkPassword(authRequest.password)
        }
    }

    private fun doValidate(request: AdminChangePasswordRequest) {
        validation(request) {
            checkLogin(request.login)
            checkPassword(request.password)
        }
    }

    private fun doValidate(registerRequest: RegisterRequest) {
        validation(registerRequest) {
            checkLogin(registerRequest.login)
            checkPassword(registerRequest.password)
            checkPassCode(registerRequest.passCode)
        }
    }


    private fun validation(input: Any, block:ValidationResult.() -> Unit) {
        val result = ValidationResult(ArrayList())
        block(result)
        if (result.errors.isNotEmpty()) {
            throw DataException("validation failed on input: ${input}, reason: ${result.errors}")
        }
    }

    private fun ValidationResult.checkLogin(login: String) {
        if (login.isEmpty()) errors.add(ValidationError("login", "is empty"))
        if (!login.matches(Regex("[A-z0-9]+"))) errors.add(ValidationError("login", "contains invalid characters"))
        if (login.length > 15) errors.add(ValidationError("login", "is too long"))
    }

    private fun ValidationResult.checkPassword(password: String) {
        if (password.isEmpty()) errors.add(ValidationError("password", "is empty"))
        if (password.length > 30) errors.add(ValidationError("password", "is too long"))
    }

    private fun ValidationResult.checkPassCode(passCode: String?) {
        if (passCode != null && passCode.length > 100) errors.add(ValidationError("passCode", "is too long"))
    }

    private fun ValidationResult.checkMessage(message: String) {
        if (message.trim().length < 10) errors.add(ValidationError("message", "is too short"))
        if (message.length > 2000) errors.add(ValidationError("message", "is too long"))
    }

    private fun ValidationResult.checkPeriod(period: String) {
        if (period.isEmpty()) errors.add(ValidationError("period", "is empty"))
        if (period.length > 20) errors.add(ValidationError("period", "is too long"))
    }

    private fun ValidationResult.checkTimezoneOffset(offset: Int) {
        if (offset < -840 || offset > 720) errors.add(ValidationError("timezoneOffset", "is out of bounds"))
    }

    data class ValidationError(val field: String, val message: String)

    data class ValidationResult(val errors: MutableList<ValidationError>)
}

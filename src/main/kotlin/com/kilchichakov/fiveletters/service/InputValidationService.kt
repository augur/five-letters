package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import org.springframework.stereotype.Service

@Service
class InputValidationService {

    fun validate(input: Any) {
        when(input) {
            is AuthRequest -> doValidate(input)
            else -> LOG.warn { "no validation rules for $input" }
        }
    }

    private fun doValidate(authRequest: AuthRequest) {
        validation {
            checkLogin(authRequest.login)
            checkPassword(authRequest.password)
        }
    }


    private fun validation(block:ValidationResult.() -> Unit) {
        val result = ValidationResult(ArrayList())
        block(result)
        if (result.errors.isNotEmpty()) {
            throw DataException("validation failed: ${result.errors}")
        }
    }

    private fun ValidationResult.checkLogin(login: String) {
        if (login.isEmpty()) errors.add(ValidationError("login", "is empty"))
        if (!login.matches(Regex("[A-z]+"))) errors.add(ValidationError("login", "contains invalid characters"))
        if (login.length > 15) errors.add(ValidationError("login", "is too long"))
    }

    private fun ValidationResult.checkPassword(password: String) {
        if (password.isEmpty()) errors.add(ValidationError("password", "is empty"))
        if (password.length > 30) errors.add(ValidationError("password", "is too long"))
    }
}
data class ValidationError(val field: String, val message: String)

data class ValidationResult(val errors: MutableList<ValidationError>)

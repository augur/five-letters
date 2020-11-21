package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.dto.AdminChangePasswordRequest
import com.kilchichakov.fiveletters.model.dto.AuthRefreshRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.model.dto.RegisterRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterFreeDateRequest
import com.kilchichakov.fiveletters.model.dto.SendLetterRequest
import com.kilchichakov.fiveletters.model.dto.UpdateProfileRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone

@Service
class InputValidationService {

    private val availableZones = TimeZone.getAvailableIDs().toSet()

    fun validate(input: Any) {
        when(input) {
            is SendLetterRequest -> doValidate(input)
            is AuthRequest -> doValidate(input)
            is RegisterRequest -> doValidate(input)
            is AdminChangePasswordRequest -> doValidate(input)
            is PageRequest -> doValidate(input)
            is UpdateProfileRequest -> doValidate(input)
            is SendLetterFreeDateRequest -> doValidate(input)
            is AuthRefreshRequest -> doValidate(input)
            else -> LOG.warn { "no validation rules for $input" }
        }
    }

    private fun doValidate(letterRequest: SendLetterRequest) {
        validation(letterRequest) {
            checkMessage(letterRequest.message)
            checkPeriod(letterRequest.period)
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
            checkEmail(registerRequest.email)
            checkTimeZone(registerRequest.timeZone)
        }
    }

    private fun doValidate(pageRequest: PageRequest) {
        validation(pageRequest) {
            checkPageNumber(pageRequest.pageNumber)
            checkPageSize(pageRequest.pageSize)
            checkInboxSortBy(pageRequest.sortBy)
        }
    }

    private fun doValidate(updateProfileRequest: UpdateProfileRequest) {
        validation(updateProfileRequest) {
            checkEmail(updateProfileRequest.email)
            checkNickname(updateProfileRequest.nickname)
            checkTimeZone(updateProfileRequest.timeZone)
        }
    }

    private fun doValidate(sendLetterFreeDateRequest: SendLetterFreeDateRequest) {
        validation(sendLetterFreeDateRequest) {
            checkMessage(sendLetterFreeDateRequest.message)
            checkOpenDate(sendLetterFreeDateRequest.openDate)
        }
    }

    private fun doValidate(authRefreshRequest: AuthRefreshRequest) {
        validation(authRefreshRequest) {
            checkLogin(authRefreshRequest.login)
            checkRefreshToken(authRefreshRequest.refreshToken)
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

    private fun ValidationResult.checkPageNumber(pageNumber: Int) {
        if (pageNumber < 1) errors.add(ValidationError("pageNumber", "is less than 1"))
    }

    private fun ValidationResult.checkPageSize(pageSize: Int) {
        if (pageSize < 1) errors.add(ValidationError("pageSize", "is less than 1"))
        if (pageSize > 100) errors.add(ValidationError("pageSize", "is too big"))
    }

    private val enabledSorts = setOf("sendDate", "openDate")
    private fun ValidationResult.checkInboxSortBy(field: String?) {
        if (field != null && !enabledSorts.contains(field)) errors.add(ValidationError("sortBy", "is invalid"))
    }

    private fun ValidationResult.checkEmail(email: String) {
        if (email.isEmpty()) errors.add(ValidationError("email", "is empty"))
        if (!email.matches(Regex(EMAIL_REGEX))) errors.add(ValidationError("email", "has invalid format"))
        if (email.length > 100) errors.add(ValidationError("email", "is too long"))
    }

    private fun ValidationResult.checkTimeZone(timeZone: String) {
        if (!availableZones.contains(timeZone)) errors.add(ValidationError("timeZone", "not recognized"))
    }

    private fun ValidationResult.checkNickname(nickname: String) {
        if (nickname.isEmpty()) errors.add(ValidationError("nickname", "is empty"))
        if (nickname.length > 30) errors.add(ValidationError("nickname", "is too long"))
    }

    private fun ValidationResult.checkOpenDate(openDate: Day) {
        val localDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        val today = Day(localDate.year.toShort(), localDate.monthValue.toByte(), localDate.dayOfMonth.toByte())
        if (openDate <= today) errors.add(ValidationError("openDate", "not in future"))
    }

    private fun ValidationResult.checkRefreshToken(refreshToken: String) {
        if (refreshToken.length > 100) errors.add(ValidationError("refreshToken", "is too long"))
    }

    data class ValidationError(val field: String, val message: String)

    data class ValidationResult(val errors: MutableList<ValidationError>)

    private val EMAIL_REGEX = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
}

package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.exception.BackendException
import com.kilchichakov.fiveletters.exception.ErrorCode.GENERIC_ERROR
import com.kilchichakov.fiveletters.exception.ErrorCode.NO_ERROR
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder


object ControllerUtils {

    fun getLogin(): String? {
        return (SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken).name
    }

    fun processAndRespondCode(authorized: Boolean = true, block: (String?) -> Unit): OperationCodeResponse {
        return try {
            val login = if (authorized) getLogin() else null
            block(login)
            OperationCodeResponse(NO_ERROR.numeric)
        } catch (e: BackendException) {
            OperationCodeResponse(e.errorCode.numeric, e.message)
        } catch (e: Exception) {
            OperationCodeResponse(GENERIC_ERROR.numeric, e.message)
        }
    }
}
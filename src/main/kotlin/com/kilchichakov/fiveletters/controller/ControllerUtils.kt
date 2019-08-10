package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.exception.BackendException
import com.kilchichakov.fiveletters.exception.ErrorCode.GENERIC_ERROR
import com.kilchichakov.fiveletters.exception.ErrorCode.NO_ERROR
import com.kilchichakov.fiveletters.model.dto.OperationCodeResponse


object ControllerUtils {

    fun processAndRespondCode(block: () -> Unit): OperationCodeResponse {
        return try {
            block()
            OperationCodeResponse(NO_ERROR.numeric)
        } catch (e : BackendException) {
            OperationCodeResponse(e.errorCode.numeric, e.message)
        } catch (e : Exception) {
            OperationCodeResponse(GENERIC_ERROR.numeric, e.message)
        }
    }
}
package com.kilchichakov.fiveletters.exception

sealed class BackendException(
        val errorCode: ErrorCode,
        message: String,
        cause: Throwable? = null
) : RuntimeException(message, cause)


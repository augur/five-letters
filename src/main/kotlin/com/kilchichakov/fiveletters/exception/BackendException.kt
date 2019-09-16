package com.kilchichakov.fiveletters.exception

sealed class BackendException(
        val errorCode: ErrorCode,
        message: String,
        cause: Throwable? = null
) : RuntimeException(message, cause)

class DatabaseException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.DB, message, cause)

class SystemStateException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.STATE, message, cause)
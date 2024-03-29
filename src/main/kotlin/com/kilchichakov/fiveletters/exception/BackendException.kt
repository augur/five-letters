package com.kilchichakov.fiveletters.exception

sealed class BackendException(
        val errorCode: ErrorCode,
        message: String,
        cause: Throwable? = null
) : RuntimeException(message, cause)

class DatabaseException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.DB, message, cause)

class SystemStateException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.STATE, message, cause)

class TermsOfUseException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.TOU, message, cause)

class DataException(message: String, cause: Throwable? = null) : BackendException(ErrorCode.DATA, message, cause)

class ExternalServiceException(message: String, cause: Throwable?) : BackendException(ErrorCode.EXTERNAL_SRV, message, cause)

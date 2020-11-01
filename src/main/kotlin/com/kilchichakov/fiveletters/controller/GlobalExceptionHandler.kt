package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.BackendException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BackendException::class)
    fun handleBackendException(e: BackendException): ResponseEntity<BackendException> {
        return ResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleAuthException(e: BadCredentialsException): ResponseEntity<ExceptionResponse> {
        return ResponseEntity(ExceptionResponse(Instant.now(), e.message), HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleGenericException(e: RuntimeException): ResponseEntity<ExceptionResponse> {
        LOG.error { "Handling generic exception: $e" }
        return ResponseEntity(ExceptionResponse(Instant.now(), e::class.simpleName), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ExceptionResponse(
        val timestamp: Instant,
        val message: String?
    )
}
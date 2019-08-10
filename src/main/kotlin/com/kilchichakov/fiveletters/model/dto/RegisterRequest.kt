package com.kilchichakov.fiveletters.model.dto

data class RegisterRequest(
        val login: String,
        val password: String,
        val acceptLicense: Boolean,
        val passCode: String?
)
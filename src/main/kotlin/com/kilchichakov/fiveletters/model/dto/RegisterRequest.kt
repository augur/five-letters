package com.kilchichakov.fiveletters.model.dto

data class RegisterRequest(
        val login: String,
        val password: String,
        val acceptLicense: Boolean,
        val passCode: String?,
        val email: String
) {
    override fun toString(): String {
        return "RegisterRequest(login='$login', password=********, acceptLicense=$acceptLicense, passCode=$passCode, email=$email)"
    }
}
package com.kilchichakov.fiveletters.model.dto

data class AuthRefreshRequest(
        val login: String,
        val refreshToken: String
) {
    override fun toString(): String {
        return "AuthRefreshRequest(login='$login', refreshToken(hash)=${refreshToken.hashCode()})"
    }
}
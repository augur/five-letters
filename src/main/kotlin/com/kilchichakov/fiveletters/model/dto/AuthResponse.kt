package com.kilchichakov.fiveletters.model.dto

import java.util.Date

data class AuthResponse(
        val login: String,
        val jwt: String,
        val jwtDueDate: Date,
        val refreshToken: String,
        val refreshTokenDueDate: Date,
) {
    override fun toString(): String {
        return "AuthResponse(login='$login', jwt(hash)=${jwt.hashCode()}, jwtDueDate=$jwtDueDate, " +
                "refreshToken(hash)=${refreshToken.hashCode()}, refreshTokenDueDate=$refreshTokenDueDate)"
    }
}
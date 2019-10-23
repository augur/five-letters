package com.kilchichakov.fiveletters.model.dto

data class AuthResponse(val jwt: String) {
    override fun toString(): String {
        return "AuthResponse(jwt(hash)='${jwt.hashCode()}')"
    }
}
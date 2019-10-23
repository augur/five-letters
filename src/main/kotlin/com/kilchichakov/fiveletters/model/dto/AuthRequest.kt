package com.kilchichakov.fiveletters.model.dto

data class AuthRequest(val login: String, val password: String) {
    override fun toString(): String {
        return "AuthRequest(login='$login', password=********)"
    }
}
package com.kilchichakov.fiveletters.model.dto

data class AdminChangePasswordRequest(val login: String, val password: String) {
    override fun toString(): String {
        return "AdminChangePasswordRequest(login='$login', password=********)"
    }
}
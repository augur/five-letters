package com.kilchichakov.fiveletters.model.dto

data class WhoAmIResponse(
        val nickname: String,
        val email: String,
        val emailConfirmed: Boolean
)
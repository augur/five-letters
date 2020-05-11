package com.kilchichakov.fiveletters.model.dto

data class SendLetterRequest(
        val message: String,
        val period: String
) {
    override fun toString(): String {
        return "SendLetterRequest(message.length=${message.length}, period='$period')"
    }
}
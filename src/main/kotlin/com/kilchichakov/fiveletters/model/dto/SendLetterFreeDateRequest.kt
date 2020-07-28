package com.kilchichakov.fiveletters.model.dto

import com.kilchichakov.fiveletters.model.Day

data class SendLetterFreeDateRequest(
    val message: String,
    val openDate: Day
) {
    override fun toString(): String {
        return "SendLetterFreeDateRequest(message.length=${message.length}, openDate=$openDate)"
    }
}
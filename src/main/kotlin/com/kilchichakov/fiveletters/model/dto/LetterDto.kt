package com.kilchichakov.fiveletters.model.dto

import java.util.Date

data class LetterDto(
        val id: String,
        val sendDate: Date,
        val openDate: Date,
        val message: String,
        val read: Boolean,
        val mailed: Boolean,
        val archived: Boolean
) {
    override fun toString(): String {
        return "LetterDto(id='$id', sendDate=$sendDate, openDate=$openDate, message.length=${message.length}, read=$read, mailed=$mailed, archived=$archived)"
    }
}
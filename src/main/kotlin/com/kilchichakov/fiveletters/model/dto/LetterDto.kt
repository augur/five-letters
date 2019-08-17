package com.kilchichakov.fiveletters.model.dto

import java.util.Date

data class LetterDto(
        val id: String,
        val date: Date,
        val message: String
)
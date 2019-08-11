package com.kilchichakov.fiveletters.model.dto

import com.kilchichakov.fiveletters.model.LetterPeriodType

data class SendLetterRequest(
        val message: String,
        val period: LetterPeriodType
)
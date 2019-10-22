package com.kilchichakov.fiveletters.model.dto

import com.kilchichakov.fiveletters.model.TimePeriod

data class SendLetterRequest(
        val message: String,
        val period: String,
        val timezoneOffset: Int = 0
)
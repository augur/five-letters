package com.kilchichakov.fiveletters.model

data class TimePeriod(
        val _id: String,
        val days: Int,
        val weeks: Int,
        val months: Int,
        val years: Int,
        val enabled: Boolean
)
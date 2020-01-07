package com.kilchichakov.fiveletters.model.job

import java.util.Date

data class JobSchedule(val nextExecutionTime: Date,
                       val repeatMode: RepeatMode,
                       val repeatInterval: Long)

enum class RepeatMode {
        ALWAYS,
        ON_FAIL,
        NEVER,
}
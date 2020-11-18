package com.kilchichakov.fiveletters.util

import java.text.SimpleDateFormat
import java.time.Clock
import java.util.Date

fun Clock.now(): Date =
        Date.from(this.instant())

fun getDateTime(s: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    return format.parse(s)
}
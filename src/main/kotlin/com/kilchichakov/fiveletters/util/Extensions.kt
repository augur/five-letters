package com.kilchichakov.fiveletters.util

import java.time.Clock
import java.util.Date

fun Clock.now(): Date =
        Date.from(this.instant())

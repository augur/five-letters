package com.kilchichakov.fiveletters.model

import java.time.Instant

interface Lock {

    fun expires(): Instant
}
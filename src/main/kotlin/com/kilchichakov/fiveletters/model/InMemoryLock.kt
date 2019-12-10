package com.kilchichakov.fiveletters.model

import java.time.Instant

data class InMemoryLock(val id: Long, var expiresAt: Instant): Lock {
    override fun expires() = expiresAt
}
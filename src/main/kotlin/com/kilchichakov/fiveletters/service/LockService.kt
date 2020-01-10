package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.Lock

interface LockService {

    fun tryLock(obj: Any): Lock?

    fun lock(obj: Any): Lock

    fun renew(lock: Lock)

    fun unlock(lock: Lock)
}
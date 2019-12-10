package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.model.InMemoryLock
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import com.kilchichakov.fiveletters.model.Lock as MyLock

@Service
class InMemoryLockService : LockService {

    val locks = ConcurrentHashMap<Long, ReentrantLock>()

    override fun lock(obj: Any): MyLock {
        val id = obj.hashCode().toLong()
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        lock.lock()
        return InMemoryLock(id, Instant.MAX)
    }

    override fun renew(lock: MyLock) {
        lock as InMemoryLock
        lock.expiresAt = Instant.MAX
    }

    override fun unlock(lock: MyLock) {
        lock as InMemoryLock
        val savedLock = locks[lock.id] ?: throw SystemStateException("lock $lock not found")
        savedLock.unlock()
    }
}
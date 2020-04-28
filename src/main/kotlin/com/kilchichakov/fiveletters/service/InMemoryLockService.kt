package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.InMemoryLock
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import com.kilchichakov.fiveletters.model.Lock as MyLock

@Service
class InMemoryLockService : LockService {

    val locks = ConcurrentHashMap<Long, ReentrantLock>()

    override fun tryLock(obj: Any): MyLock? {
        val id = obj.hashCode().toLong()
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        if (lock.tryLock()) {
            return InMemoryLock(id, Instant.MAX)
        }
        return null
    }

    override fun lock(obj: Any): MyLock {
        val id = obj.hashCode().toLong()
        val lock = locks.computeIfAbsent(id) { ReentrantLock() }
        lock.lock()
        locks[id] = lock
        return InMemoryLock(id, Instant.MAX)
    }

    override fun renew(lock: MyLock) {
        lock as InMemoryLock
        lock.expiresAt = Instant.MAX
    }

    override fun unlock(lock: MyLock) {
        lock as InMemoryLock
        locks.remove(lock.id)?.unlock()
    }
}
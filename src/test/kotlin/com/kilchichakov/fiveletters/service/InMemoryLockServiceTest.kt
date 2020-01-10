package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.Lock
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

@ExtendWith(MockKExtension::class)
internal class InMemoryLockServiceTest {

    @InjectMockKs
    lateinit var service: InMemoryLockService

    // This test does more than just unit testing, but it is because of service specificity
    @Test
    fun `should do locking and unlocking`() {
        // Given
        val entity = "loupa"
        val counter = AtomicInteger(1)
        val results = ConcurrentLinkedQueue<Boolean>()
        // When
        val testBlock = { x: Int ->
            val lock = service.lock(entity)
            val initial = counter.get()
            Thread.sleep(50)
            results.add(counter.addAndGet(x) == initial + x)
            service.unlock(lock)
        }
        val increments = listOf(2, 4, 8, 16, 32, 64)
        increments
                .map { thread { testBlock(it) } }
                .forEach { it.join() }

        assert(results.stream().allMatch { it == true })
    }

    @Test
    fun `should try locking`() {
        // Given
        val entity = "loupa"

        // When
        val lock1: Lock? = service.tryLock(entity)
        var lock2: Lock? = null
        thread {
            lock2 = service.tryLock(entity)
        }.join()

        // Then
        assertThat(lock1).isNotNull
        assertThat(lock2).isNull()
    }
}
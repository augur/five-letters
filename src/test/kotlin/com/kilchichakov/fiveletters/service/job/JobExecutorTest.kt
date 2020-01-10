package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.model.Lock
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.service.JobService
import com.kilchichakov.fiveletters.service.LockService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class JobExecutorTest {

    @RelaxedMockK
    lateinit var clock: Clock

    @RelaxedMockK
    lateinit var jobService: JobService

    @RelaxedMockK
    lateinit var lockService: LockService

    @InjectMockKs
    lateinit var executor: JobExecutor

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)

    @Test
    fun `should execute ready jobs`() {
        // Given
        every { clock.instant() } returns instant
        val jobId1 = mockk<ObjectId>()
        val jobId2 = mockk<ObjectId>()
        val jobId3 = mockk<ObjectId>()
        val jobId4 = mockk<ObjectId>()
        every { jobService.getReadyJobs() } returns listOf(jobId1, jobId2, jobId3, jobId4)
        val job1 = mockk<Job>()
        val job2 = mockk<Job>()
        val job3 = mockk<Job>()
        val job4 = mockk<Job>()
        every { jobService.getJob(jobId1) } returns job1
        every { jobService.getJob(jobId2) } returns null
        every { jobService.getJob(jobId3) } returns job3
        every { jobService.getJob(jobId4) } returns job4
        val job2lock = mockk<Lock>()
        val job3lock = mockk<Lock>()
        val job4lock = mockk<Lock>()
        every { lockService.tryLock(jobId1) } returns null
        every { lockService.tryLock(jobId2) } returns job2lock
        every { lockService.tryLock(jobId3) } returns job3lock
        every { lockService.tryLock(jobId4) } returns job4lock
        every { job3.status } returns JobStatus.ACTIVE
        every { job4.status } returns JobStatus.ACTIVE
        every { job3.schedule.nextExecutionTime } returns Date.from(instant.plusMillis(100))
        every { job4.schedule.nextExecutionTime } returns Date.from(instant)

        // When
        executor.executeJobs()

        // Then
        verify {
            jobService.getReadyJobs()
            lockService.tryLock(jobId1)
            lockService.tryLock(jobId2)
            jobService.getJob(jobId2)
            lockService.tryLock(jobId3)
            jobService.getJob(jobId3)
            lockService.tryLock(jobId4)
            jobService.getJob(jobId4)
            jobService.serve(job4)
            lockService.unlock(job2lock)
            lockService.unlock(job3lock)
            lockService.unlock(job4lock)
        }
        confirmVerified(jobService, lockService)
    }

}
package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobPayload
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.model.job.RepeatMode
import com.kilchichakov.fiveletters.model.job.TestJobPayload
import com.kilchichakov.fiveletters.repository.JobRepository
import com.mongodb.client.ClientSession
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.util.Date
import java.util.UUID

@ExtendWith(MockKExtension::class)
internal class JobServiceTest {

    @RelaxedMockK
    lateinit var transactionWrapper: TransactionWrapper

    @RelaxedMockK
    lateinit var jobRepository: JobRepository

    @RelaxedMockK
    lateinit var userDataService: UserService

    @RelaxedMockK
    lateinit var clock: Clock

    @InjectMockKs
    lateinit var service: JobService

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should schedule email confirmation job`() {
        // Given
        val session = mockk<ClientSession>()
        every { transactionWrapper.executeInTransaction(any()) } answers {
            firstArg<(ClientSession)->Any>().invoke(session)
        }
        val date = Date.from(instant)
        val login = "loupa"
        val email = "poupa"
        val code = "someCode"
        val scheduled = slot<Job>()
        val userData = mockk<UserData>()
        every { userData.email } returns email
        every { userDataService.loadUserData(login) } returns userData
        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns code
        every { clock.instant() } returns instant

        every { jobRepository.insertJob(capture(scheduled), any()) } just runs

        // When
        service.scheduleEmailConfirmation(login)

        // Then
        val payload = scheduled.captured.payload as EmailConfirmSendingJobPayload
        assertThat(scheduled.captured.status).isEqualTo(JobStatus.ACTIVE)
        assertThat(scheduled.captured.schedule).isEqualTo(JobSchedule(date, RepeatMode.ON_FAIL, 60000))
        assertThat(payload.email).isEqualTo(email)
        assertThat(payload.code).isEqualTo(code)

        verify { userDataService.setConfirmationCode(login, code, session) }
    }

    @Test
    fun `should serve task`() {
        // Given
        val date = Date.from(instant)
        val interval = 100L
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["executePayload"](any<JobPayload>()) } returns true
        val id = ObjectId()
        val schedule = JobSchedule(date, RepeatMode.ON_FAIL, interval)
        val payload = TestJobPayload("ddd")
        val status = JobStatus.ACTIVE
        val job = Job(id, schedule, payload, status)

        // When
        spy.serve(job)

        // Then
        verify {
            spy["executePayload"](payload)
            jobRepository.setJobStatus(id, JobStatus.DONE, schedule)
        }
    }

    @Test
    fun `should not serve non-active tasks`() {
        // Given
        val date = Date.from(instant)
        val interval = 100L
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["executePayload"](any<JobPayload>()) } returns true
        val id = ObjectId()
        val schedule = JobSchedule(date, RepeatMode.ON_FAIL, interval)
        val jobDone = Job(id, schedule, TestJobPayload("ddd"), JobStatus.DONE)
        val jobFailed = Job(id, schedule, TestJobPayload("ddd"), JobStatus.FAILED)

        // When
        spy.serve(jobDone)
        spy.serve(jobFailed)

        // Then
        verify(exactly = 0) { spy["executePayload"](any<JobPayload>()) }
    }

    @Test
    fun `should reschedule task if succcesful and always repeat`() {
        // Given
        val date = Date.from(instant)
        val interval = 100L
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["executePayload"](any<JobPayload>()) } returns true
        val id = ObjectId()
        val schedule = JobSchedule(date, RepeatMode.ALWAYS, interval)
        val payload = TestJobPayload("ddd")
        val status = JobStatus.ACTIVE
        val job = Job(id, schedule, payload, status)
        val newSchedule = slot<JobSchedule>()
        every { jobRepository.setJobStatus(any(), any(), capture(newSchedule)) } returns true

        // When
        spy.serve(job)

        // Then
        assertThat(newSchedule.captured.repeatMode).isEqualTo(RepeatMode.ALWAYS)
        assertThat(newSchedule.captured.repeatInterval).isEqualTo(100L)
        assertThat(newSchedule.captured.nextExecutionTime).isEqualTo(Date.from(instant.plusMillis(100L)))
        verify {
            spy["executePayload"](payload)
            jobRepository.setJobStatus(id, JobStatus.ACTIVE, newSchedule.captured)
        }
    }

    @Test
    fun `should reschedule task if failed`() {
        // Given
        val date = Date.from(instant)
        val interval = 100L
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["executePayload"](any<JobPayload>()) } returns false
        val id = ObjectId()
        val schedule = JobSchedule(date, RepeatMode.ON_FAIL, interval)
        val payload = TestJobPayload("ddd")
        val status = JobStatus.ACTIVE
        val job = Job(id, schedule, payload, status)
        val newSchedule = slot<JobSchedule>()
        every { jobRepository.setJobStatus(any(), any(), capture(newSchedule)) } returns true

        // When
        spy.serve(job)

        // Then
        assertThat(newSchedule.captured.repeatMode).isEqualTo(RepeatMode.ON_FAIL)
        assertThat(newSchedule.captured.repeatInterval).isEqualTo(100L)
        assertThat(newSchedule.captured.nextExecutionTime).isEqualTo(Date.from(instant.plusMillis(100L)))
        verify {
            spy["executePayload"](payload)
            jobRepository.setJobStatus(id, JobStatus.ACTIVE, newSchedule.captured)
        }
    }

    @Test
    fun `should update task as failed if not repeatable`() {
        // Given
        val date = Date.from(instant)
        val interval = 100L
        val spy = spyk(service, recordPrivateCalls = true)
        every { spy["executePayload"](any<JobPayload>()) } returns false
        val id = ObjectId()
        val schedule = JobSchedule(date, RepeatMode.NEVER, interval)
        val payload = TestJobPayload("ddd")
        val status = JobStatus.ACTIVE
        val job = Job(id, schedule, payload, status)

        // When
        spy.serve(job)

        // Then
        verify {
            spy["executePayload"](payload)
            jobRepository.setJobStatus(id, JobStatus.FAILED, schedule)
        }
    }
}
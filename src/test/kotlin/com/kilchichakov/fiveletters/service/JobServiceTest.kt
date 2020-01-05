package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
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
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(scheduled.captured.schedule).isEqualTo(JobSchedule(date))
        assertThat(payload.email).isEqualTo(email)
        assertThat(payload.code).isEqualTo(code)

        verify { userDataService.setConfirmationCode(login, code, session) }
    }
}
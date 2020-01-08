package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobPayload
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.model.job.RepeatMode
import com.kilchichakov.fiveletters.model.job.TestJobPayload
import com.kilchichakov.fiveletters.repository.JobRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import com.kilchichakov.fiveletters.service.job.EmailConfirmJobProcessor
import com.kilchichakov.fiveletters.util.now
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.Date
import java.util.UUID

@Service
class JobService {

    private val EMAIL_CONFIRM_REPEAT_INTERVAL = 60000L

    @Autowired
    private lateinit var clock: Clock

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jobRepository: JobRepository

    @Autowired
    private lateinit var transactionWrapper: TransactionWrapper

    @Autowired
    private lateinit var emailConfirmProcessor: EmailConfirmJobProcessor

    fun scheduleEmailConfirmation(login: String) {
        LOG.info { "scheduling email confirmation for $login" }

        transactionWrapper.executeInTransaction { session ->
            val userData = userService.loadUserData(login)

            val code = generateUUID()
            val payload = EmailConfirmSendingJobPayload(userData.email!!, code)
            val job = Job(null, JobSchedule(clock.now(), RepeatMode.ON_FAIL, EMAIL_CONFIRM_REPEAT_INTERVAL), payload)

            userService.setConfirmationCode(login, code, session)
            jobRepository.insertJob(job, session)
        }

        LOG.info { "scheduled" }
    }

    fun getReadyJobs(): List<Job> {
        return jobRepository.loadReadyJobs()
    }

    fun serve(job: Job) {
        LOG.info { "serving job $job" }
        if (job.status != JobStatus.ACTIVE) {
            LOG.error { "job status is not active, skipping" }
            return
        }

        LOG.info { "executing payload ${job.payload}" }
        if (executePayload(job.payload)) {
            when (job.schedule.repeatMode) {
                RepeatMode.ON_FAIL, RepeatMode.NEVER -> {
                    LOG.info { "updating job status DONE" }
                    jobRepository.setJobStatus(job._id!!, JobStatus.DONE, job.schedule)
                }
                RepeatMode.ALWAYS -> {
                    LOG.info { "rescheduling successful job" }
                    reschedule(job)
                }
            }

        } else {
            when (job.schedule.repeatMode) {
                RepeatMode.ON_FAIL, RepeatMode.ALWAYS -> {
                    LOG.info { "rescheduling failed job" }
                    reschedule(job)
                }
                RepeatMode.NEVER -> {
                    LOG.info { "updating job status FAILED" }
                    jobRepository.setJobStatus(job._id!!, JobStatus.FAILED, job.schedule)
                }
            }
        }

        LOG.info { "served" }
    }


    private fun executePayload(payload: JobPayload): Boolean {
        return try {
            when(payload) {
                is TestJobPayload -> {
                    LOG.info { "payload type: TestJobPayload" }
                    if (payload.data.isEmpty()) throw DataException("TestPayload data is empty")
                }
                is EmailConfirmSendingJobPayload -> {
                    LOG.info { "payload type: EmailConfirmSendingJobPayload" }
                    emailConfirmProcessor.process(payload)
                }
            }
            true
        } catch (e: Exception) {
            LOG.error { "caught $e" }
            false
        }
    }

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

    private fun reschedule(job: Job) {
        val newSchedule = job.schedule.copy(nextExecutionTime = calcNextTime(job.schedule))
        jobRepository.setJobStatus(job._id!!, JobStatus.ACTIVE, newSchedule)
    }

    private fun calcNextTime(schedule: JobSchedule): Date {
        return Date.from(schedule.nextExecutionTime.toInstant().plusMillis(schedule.repeatInterval))
    }
}
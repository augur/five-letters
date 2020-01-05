package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.repository.JobRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import com.kilchichakov.fiveletters.util.now
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.UUID

@Service
class JobService {

    @Autowired
    private lateinit var clock: Clock

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jobRepository: JobRepository

    @Autowired
    private lateinit var transactionWrapper: TransactionWrapper

    fun serve(job: Job) {
        TODO()
    }

    fun scheduleEmailConfirmation(login: String) {
        LOG.info { "scheduling email confirmation for $login" }

        transactionWrapper.executeInTransaction { session ->
            val userData = userService.loadUserData(login)

            val code = generateUUID()
            val payload = EmailConfirmSendingJobPayload(userData.email!!, code)
            val job = Job(null, JobSchedule(clock.now()), payload)

            userService.setConfirmationCode(login, code, session)
            jobRepository.insertJob(job, session)
        }

        LOG.info { "scheduled" }
    }

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }
}
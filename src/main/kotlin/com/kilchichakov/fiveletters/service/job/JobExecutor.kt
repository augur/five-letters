package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Lock
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.service.JobService
import com.kilchichakov.fiveletters.service.LockService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class JobExecutor(
        private val clock: Clock
) {

    @Autowired
    private lateinit var jobService: JobService

    @Autowired
    private lateinit var lockService: LockService

    @Scheduled(fixedRate = 10000)
    @Logged
    fun executeJobs() {
        LOG.debug { "executing jobs" }

        val jobIds = jobService.getReadyJobs()

        for (id in jobIds) {
            var lock: Lock? = null
            try {
                lock = lockService.tryLock(id)
                if (lock != null) {
                    val job = jobService.getJob(id) ?: throw DatabaseException("Couldn't find job by id $id")
                    if (job.status == JobStatus.ACTIVE && clock.instant() >= job.schedule.nextExecutionTime.toInstant()) {
                        jobService.serve(job)
                    }
                }
            } catch (e: Exception) {
                LOG.error { "caught $e during job $id execution" }
            } finally {
                if (lock != null) lockService.unlock(lock)
            }
        }

        LOG.debug { "done" }
    }
}
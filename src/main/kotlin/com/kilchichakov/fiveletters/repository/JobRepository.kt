package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.util.now
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.lte
import org.litote.kmongo.setValue
import org.springframework.stereotype.Repository
import java.time.Clock

@Repository
class JobRepository(
        db: MongoDatabase,
        private val clock: Clock
) {

    private val collection = db.getCollection<Job>()


    fun insertJob(job: Job) {
        LOG.info { "inserting new job $job" }
        collection.insertOne(job)
        LOG.info { "saved" }
    }

    fun loadReadyJobs(): List<Job> {
        LOG.info { "load ready to execute jobs" }
        val byStatus = Job::status eq JobStatus.ACTIVE
        val byNextTime = Job::schedule / JobSchedule::nextExecutionTime lte clock.now()
        val found = collection.find(and(byStatus, byNextTime))
        LOG.info { "found ${found.count()} ready jobs" }
        return found.toList().sortedBy { it.schedule.nextExecutionTime }
    }

    fun setJobStatus(id: ObjectId, status: JobStatus, schedule: JobSchedule): Boolean {
        LOG.info { "updating job $id with $status and $schedule"}
        val byId = Job::_id eq id
        val update = and(setValue(Job::status, status), setValue(Job::schedule, schedule))
        val result = collection.updateOne(byId, update)
        LOG.info { "updated ${result.modifiedCount} jobs" }
        return result.modifiedCount == 1L
    }
}

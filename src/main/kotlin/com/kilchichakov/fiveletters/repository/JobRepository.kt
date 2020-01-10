package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.kilchichakov.fiveletters.util.now
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
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


    fun insertJob(job: Job, clientSession: ClientSession) {
        LOG.info { "inserting new job $job" }
        collection.insertOne(clientSession, job)
        LOG.info { "saved" }
    }

    fun loadJob(id: ObjectId): Job? {
        LOG.info { "loading job by id $id" }
        return collection.findOneById(id)
                .also { LOG.info { "found $it" }  }
    }

    fun loadReadyJobs(): List<ObjectId> {
        LOG.debug { "load ready to execute jobs" }
        val byStatus = Job::status eq JobStatus.ACTIVE
        val byNextTime = Job::schedule / JobSchedule::nextExecutionTime lte clock.now()
        val found = collection.find(and(byStatus, byNextTime))
        LOG.debug { "found ${found.count()} ready jobs" }
        return found.toList().sortedBy { it.schedule.nextExecutionTime }.map { it._id!! }
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

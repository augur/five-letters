package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.job.TestJobPayload
import com.kilchichakov.fiveletters.model.job.Job
import com.kilchichakov.fiveletters.model.job.JobSchedule
import com.kilchichakov.fiveletters.model.job.JobStatus
import com.mongodb.client.MongoCollection
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date

@ExtendWith(MockKExtension::class)
class JobRepositoryTest : MongoTestSuite() {

    private lateinit var repository: JobRepository

    private lateinit var collection: MongoCollection<Job>

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = JobRepository(db, clock)
        collection = db.getCollection()
    }

    @Test
    fun `should save new job`() {
        // Given
        val next = Date.from(instant)
        val payload = TestJobPayload("loupa")
        val schedule = JobSchedule(next)
        val job = Job(null, schedule, payload)

        // When
        transactionWrapper.executeInTransaction {
            repository.insertJob(job, it)
        }

        // Then
        val found = collection.findOne()!!
        assertThat(found).isEqualToIgnoringGivenFields(job, "_id")
        assertThat(found._id).isNotNull()
    }

    @Test
    fun `should get jobs ready to execute`() {
        // Given
        val next = Date.from(instant)
        val failed = Job(ObjectId(), JobSchedule(next), TestJobPayload("loupa"), JobStatus.FAILED)
        val done = Job(ObjectId(), JobSchedule(next), TestJobPayload("poupa"), JobStatus.DONE)
        val active = Job(ObjectId(), JobSchedule(next), TestJobPayload("foo"), JobStatus.ACTIVE)
        val future = Job(ObjectId(), JobSchedule(Date.from(instant.plusMillis(100))), TestJobPayload("bar"), JobStatus.ACTIVE)
        val past = Job(ObjectId(), JobSchedule(Date.from(instant.minusMillis(100))), TestJobPayload("baz"), JobStatus.ACTIVE)
        transactionWrapper.executeInTransaction {
            repository.insertJob(failed, it)
            repository.insertJob(done, it)
            repository.insertJob(active, it)
            repository.insertJob(future, it)
            repository.insertJob(past, it)
        }

        // When
        val actual = repository.loadReadyJobs()

        // Then
        assertThat(actual).containsExactly(past, active)
    }

    @Test
    fun `should update job status and schedule`() {
        // Given
        val id = ObjectId()
        val next = Date.from(instant)
        val payload = TestJobPayload("loupa")
        val schedule = JobSchedule(next)
        val status = JobStatus.ACTIVE
        val job = Job(id, schedule, payload, status)
        transactionWrapper.executeInTransaction {
            repository.insertJob(job, it)
        }
        val newStatus = JobStatus.DONE
        val newSchedule = JobSchedule(Date.from(instant.plusMillis(100500)))

        // When
        val actual = repository.setJobStatus(id, newStatus, newSchedule)

        // Then
        val found = collection.findOne()!!
        assertThat(found._id).isEqualTo(id)
        assertThat(found.status).isEqualTo(newStatus)
        assertThat(found.schedule).isEqualTo(newSchedule)
        assertThat(found.payload).isEqualTo(payload)
    }
}
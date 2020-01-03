package com.kilchichakov.fiveletters.model.job

import org.bson.types.ObjectId

data class Job(val _id: ObjectId?,
               val schedule: JobSchedule,
               val payload: JobPayload,
               val status: JobStatus = JobStatus.ACTIVE)


enum class JobStatus {
    ACTIVE,
    FAILED,
    DONE
}
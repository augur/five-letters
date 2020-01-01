package com.kilchichakov.fiveletters.model.job

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "jobType")
@JsonSubTypes(
        JsonSubTypes.Type(value = EmailConfirmSendingJob::class, name = "emailConfirmSend")
)
sealed class JobPayload

data class EmailConfirmSendingJob(val email: String): JobPayload()
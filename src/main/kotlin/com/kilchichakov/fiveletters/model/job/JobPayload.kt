package com.kilchichakov.fiveletters.model.job

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = TestJobPayload::class, name = "test"),
        JsonSubTypes.Type(value = EmailConfirmSendingJobPayload::class, name = "emailConfirmSend"),
        JsonSubTypes.Type(value = DailyMailingJobPayload::class, name = "dailyMailing")
)
sealed class JobPayload

data class TestJobPayload(val data: String): JobPayload()

data class EmailConfirmSendingJobPayload(val email: String, val code: String): JobPayload()

data class DailyMailingJobPayload(val data: String): JobPayload()
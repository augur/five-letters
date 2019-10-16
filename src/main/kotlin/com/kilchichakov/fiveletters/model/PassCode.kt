package com.kilchichakov.fiveletters.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.Date

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = OneTimePassCode::class, name = "otp"),
        JsonSubTypes.Type(value = OneTimePassCodeConsumed::class, name = "otpConsumed")
)
sealed class PassCode

data class OneTimePassCode(val _id: String,
                           val validUntil: Date) : PassCode()

data class OneTimePassCodeConsumed(val _id: String,
                                   val login: String,
                                   val date: Date) : PassCode()
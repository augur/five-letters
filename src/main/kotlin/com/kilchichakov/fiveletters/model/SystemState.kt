package com.kilchichakov.fiveletters.model

data class SystemState(
        val registrationEnabled: Boolean
)

val DEFAULT_STATE = SystemState(
        false
)
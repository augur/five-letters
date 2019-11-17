package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId

data class UserData(
        val _id: ObjectId?,
        val login: String,
        val nickname: String?,
        val email: String?,
        val emailConfirmed: Boolean = false)
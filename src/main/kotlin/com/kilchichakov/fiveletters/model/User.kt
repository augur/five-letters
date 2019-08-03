package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId

data class User(
        val _id: ObjectId?,
        val login: String,
        val password: String,
        val nickname: String)
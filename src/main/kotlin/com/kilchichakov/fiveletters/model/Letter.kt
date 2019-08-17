package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId
import java.util.Date

data class Letter(
        val _id: ObjectId?,
        val login: String,
        val message: String,
        val isRead: Boolean,
        val sendDate: Date,
        val openDate: Date
)
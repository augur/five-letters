package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId
import java.util.Date

data class Letter(
        val _id: ObjectId?,
        val login: String,
        val message: String,
        val read: Boolean,
        val sendDate: Date,
        val openDate: Date,
        val mailSent: Boolean = false
) {
    override fun toString(): String {
        return "Letter(_id=$_id, login='$login', message.length=${message.length}, read=$read, sendDate=$sendDate, openDate=$openDate, mailSent=$mailSent)"
    }
}
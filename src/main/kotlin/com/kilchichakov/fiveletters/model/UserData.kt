package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId

data class UserData(
        val _id: ObjectId?,
        val login: String,
        val password: String,
        val nickname: String,
        val admin: Boolean = false) {
    override fun toString(): String {
        return "UserData(_id=$_id, login='$login', password=********, nickname='$nickname', admin=$admin)"
    }
}
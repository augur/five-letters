package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.springframework.stereotype.Repository

@Repository
class UserDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<UserData>()

    fun insertNewUser(userData: UserData) {
        LOG.info { "inserting user $userData" }
        collection.insertOne(userData)
        LOG.info { "inserted" }
    }

    fun loadUserData(login: String): UserData? {
        LOG.info { "loading userData of $login" }
        return collection.findOne(UserData::login eq login).also {
            LOG.info { "found userData $it" }
        }
    }
}
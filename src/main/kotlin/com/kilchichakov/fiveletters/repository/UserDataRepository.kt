package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue
import org.springframework.stereotype.Repository

@Repository
class UserDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<UserData>()

    fun insertNewUser(userData: UserData, clientSession: ClientSession) {
        LOG.info { "inserting user $userData" }
        collection.insertOne(clientSession, userData)
        LOG.info { "inserted" }
    }

    fun loadUserData(login: String): UserData? {
        LOG.info { "loading userData of $login" }
        return collection.findOne(UserData::login eq login).also {
            LOG.info { "found userData $it" }
        }
    }

    fun changePassword(login: String, encodedPassword: String): Boolean {
        LOG.info { "updating password of $login" }
        val byLogin = UserData::login eq login
        val update = setValue(UserData::password, encodedPassword)
        val result = collection.updateOne(byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L;
    }
}
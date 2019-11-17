package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.AuthData
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue
import org.springframework.stereotype.Repository

@Repository
class AuthDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<AuthData>("userData")

    fun insertNewUser(authData: AuthData, clientSession: ClientSession) {
        LOG.info { "inserting authData $authData" }
        collection.insertOne(clientSession, authData)
        LOG.info { "inserted" }
    }

    fun loadUserData(login: String): AuthData? {
        LOG.info { "loading authData of $login" }
        return collection.findOne(AuthData::login eq login).also {
            LOG.info { "found userData $it" }
        }
    }

    fun changePassword(login: String, encodedPassword: String): Boolean {
        LOG.info { "updating password of $login" }
        val byLogin = AuthData::login eq login
        val update = setValue(AuthData::password, encodedPassword)
        val result = collection.updateOne(byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L;
    }
}
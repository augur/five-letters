package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.AuthDataByEmailSearchResult
import com.kilchichakov.fiveletters.model.FoundEmailUnconfirmed
import com.kilchichakov.fiveletters.model.FoundOk
import com.kilchichakov.fiveletters.model.NotFound
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.and
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
        return collection.findOne(AuthData::login eq login)
                .also { LOG.info { "found userData $it" } }
    }

    fun changePassword(login: String, encodedPassword: String): Boolean {
        LOG.info { "updating password of $login" }
        val byLogin = AuthData::login eq login
        val update = setValue(AuthData::password, encodedPassword)
        val result = collection.updateOne(byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L;
    }

    fun findAuthDataByEmail(email: String): AuthDataByEmailSearchResult {
        LOG.info { "searching authData by confirmed email=$email" }
        val filterConfirmed = and(UserData::email eq email, UserData::emailConfirmed eq true)
        val authData = collection.findOne(filterConfirmed)
        if (authData != null) {
            LOG.info { "found authData=$authData" }
            return FoundOk(authData)
        }
        LOG.info { "not found with confirmed email=$email, searching with unconfirmed" }
        val filter = UserData::email eq email
        val authDataUnconfirmed = collection.findOne(filter)
        if (authDataUnconfirmed != null) {
            LOG.info { "found with unconfirmed email, authData=$authDataUnconfirmed" }
            return FoundEmailUnconfirmed(authDataUnconfirmed)
        }
        LOG.info { "not found with unconfirmed email=$email" }
        return NotFound
    }
}
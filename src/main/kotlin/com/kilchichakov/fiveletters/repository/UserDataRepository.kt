package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
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
class UserDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<UserData>("userData")

    fun loadUserData(login: String): UserData? {
        LOG.info { "loading userData of $login" }
        return collection.findOne(UserData::login eq login)
                .also { LOG.info { "found userData $it" } }
    }

    data class UpdateUserDataResult(val success: Boolean, val emailChanged: Boolean)

    fun updateUserData(login: String, email: String, nickname: String): UpdateUserDataResult {
        LOG.info { "updating userData of $login - $email, $nickname" }
        val existingData =  loadUserData(login) ?: throw DatabaseException("UserData of $login not found")
        val byLogin = UserData::login eq login
        var update = setValue(UserData::email, email)
        var emailChanged = false
        if (existingData.email != email) {
            LOG.info { "email changed, setting to unconfirmed" }
            emailChanged = true
            update = and(update, setValue(UserData::emailConfirmed, false))
        }
        update = and(update, setValue(UserData::nickname, nickname))
        val result = collection.updateOne(byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return UpdateUserDataResult(result.modifiedCount == 1L, emailChanged)
    }

    fun setEmailConfirmationCode(login: String, code: String, clientSession: ClientSession): Boolean {
        LOG.info { "setting email code for $login to $code" }
        val byLogin = UserData::login eq login
        val update = and(setValue(UserData::emailConfirmed, false),
                setValue(UserData::emailConfirmationCode, code))
        val result = collection.updateOne(clientSession, byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L
    }

    fun setEmailConfirmed(code: String): Boolean {
        val nullCode: String? = null
        LOG.info { "setting email confirmed for $code" }
        val byEmailCode = UserData::emailConfirmationCode eq code
        val update = and(setValue(UserData::emailConfirmed, true),
                setValue(UserData::emailConfirmationCode, nullCode))
        val result = collection.updateOne(byEmailCode, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L
    }
}
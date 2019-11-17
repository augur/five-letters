package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.UserData
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
        return collection.findOne(UserData::login eq login).also {
            LOG.info { "found userData $it" }
        }
    }

    fun updateUserData(login: String, email: String, nickname: String): Boolean {
        LOG.info { "updating userData of $login - $email, $nickname" }
        val existingData =  loadUserData(login) ?: throw DatabaseException("UserData of $login not found")
        val byLogin = UserData::login eq login
        var update = setValue(UserData::email, email)
        if (existingData.email != email) {
            LOG.info { "email changed, setting to unconfirmed" }
            update = and(update, setValue(UserData::emailConfirmed, false))
        }
        update = and(update, setValue(UserData::nickname, nickname))
        val result = collection.updateOne(byLogin, update)
        LOG.info { "updated ${result.modifiedCount} users" }
        return result.modifiedCount == 1L;
    }

}
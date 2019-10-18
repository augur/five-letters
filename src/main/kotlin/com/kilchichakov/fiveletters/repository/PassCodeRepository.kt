package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.OneTimePassCodeConsumed
import com.kilchichakov.fiveletters.model.PassCode
import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.kilchichakov.fiveletters.util.now
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.updateOne
import org.litote.kmongo.updateOneById
import org.springframework.stereotype.Repository
import java.time.Clock

@Repository
class PassCodeRepository(
        db: MongoDatabase,
        private val clock: Clock
) {

    private val collection = db.getCollection<PassCode>()

    fun insertPassCode(passCode: PassCode) {
        collection.insertOne(passCode)
    }

    fun findPassCode(code: String): PassCode? {
        return collection.findOneById(code)
    }

    fun consumeOneTimePassCode(code: String, login: String, clientSession: ClientSession) {
        val now = clock.now()
        val passCode = collection.findOneById(clientSession, code) ?: throw DatabaseException("Not found passCode $code")
        passCode as? OneTimePassCode ?: throw DataException("Passcode $code is not OneTimePasscode")
        if (now > passCode.validUntil) throw DataException("PassCode $passCode is overdue")
        val oneTimePassCodeConsumed = OneTimePassCodeConsumed(passCode._id, login, now)
        val result = collection.updateOneById(clientSession, passCode._id, oneTimePassCodeConsumed, UpdateOptions(), updateOnlyNotNullProperties = false)
        if (result.modifiedCount != 1L) throw DatabaseException("Unexpected update result during consumeOneTimePassCode()")
    }
}
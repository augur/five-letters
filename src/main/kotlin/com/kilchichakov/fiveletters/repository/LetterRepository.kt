package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.util.now
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.gt
import org.litote.kmongo.lte
import org.litote.kmongo.orderBy
import org.litote.kmongo.setValue
import org.springframework.stereotype.Repository
import java.time.Clock

@Repository
class LetterRepository(
        db: MongoDatabase,
        private val clock: Clock
) {

    private val collection = db.getCollection<Letter>()

    fun saveNewLetter(letter: Letter) {
        LOG.info { "inserting new letter $letter" }
        collection.insertOne(letter)
        LOG.info { "inserted" }
    }

    fun getNewLetters(login: String): List<Letter> {
        LOG.info { "load new letters of user $login" }
        val byLogin = Letter::login eq login
        val byIsRead = Letter::read eq false
        val byOpenDate = Letter::openDate lte clock.now()
        val found = collection.find(and(byLogin, byIsRead, byOpenDate))
        LOG.info { "found ${found.count()} letters" }
        return found.toList()
    }

    fun getLettersForMailing(): List<Letter> {
        LOG.info { "load letters of user mail sending" }
        val byIsRead = Letter::read eq false
        val byIsMailed = Letter::mailSent eq false
        val byOpenDate = Letter::openDate lte clock.now()
        val found = collection.find(and(byIsMailed, byIsRead, byOpenDate))
        LOG.info { "found ${found.count()} letters" }
        return found.toList()    }

    fun getFutureLetters(login: String, limit: Int): List<SealedLetterEnvelop> {
        LOG.info { "load future letters of user $login, limit $limit" }
        val byLogin = Letter::login eq login
        val byOpenDate = Letter::openDate gt clock.now()
        val sorted = orderBy(Letter::openDate)
        val found = collection.find(and(byLogin, byOpenDate), SealedLetterEnvelop::class.java)
                .sort(sorted)
                .limit(limit)
        LOG.info { "found ${found.count()} letters" }
        return found.toList()
    }

    fun markLetterAsRead(login: String, id: String): Boolean {
        LOG.info { "updating letter $id of $login as read"}
        val byLogin = Letter::login eq login
        val byId = Letter::_id eq ObjectId(id)
        val update = setValue(Letter::read, true)
        val result = collection.updateOne(and(byId, byLogin), update)
        LOG.info { "updated ${result.modifiedCount} letters" }
        return result.modifiedCount == 1L
    }
}
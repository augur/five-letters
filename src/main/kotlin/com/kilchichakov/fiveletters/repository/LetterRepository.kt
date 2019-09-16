package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.util.now
import com.mongodb.client.MongoDatabase
import org.bson.types.ObjectId
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.lte
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
        collection.insertOne(letter)
    }

    fun getNewLetters(login: String): List<Letter> {
        val byLogin = Letter::login eq login
        val byIsRead = Letter::read eq false
        val byOpenDate = Letter::openDate lte clock.now()
        val found = collection.find(and(byLogin, byIsRead, byOpenDate))
        return found.toList()
    }

    fun markLetterAsRead(login: String, id: String): Boolean {
        val byLogin = Letter::login eq login
        val byId = Letter::_id eq ObjectId(id)
        val update = setValue(Letter::read, true)
        val result = collection.updateOne(and(byId, byLogin), update)
        return result.modifiedCount == 1L
    }

}
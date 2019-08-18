package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.Letter
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.lte
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
class LetterRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<Letter>()

    fun saveNewLetter(letter: Letter) {
        collection.insertOne(letter)
    }

    fun getNewLetters(login: String): List<Letter> {
        val byLogin = Letter::login eq login
        val byIsRead = Letter::isRead eq false
        val byOpenDate = Letter::openDate lte Date()
        val found = collection.find(and(byLogin, byIsRead, byOpenDate))
        return found.toList()
    }

}
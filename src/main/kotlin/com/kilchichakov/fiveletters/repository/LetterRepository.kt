package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.Letter
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.getCollection
import org.springframework.stereotype.Repository

@Repository
class LetterRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<Letter>()

    fun saveNewLetter(letter: Letter) {
        collection.insertOne(letter)
    }
}
package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.User
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.getCollection
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct

@Repository
class UserRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<User>()




    @PostConstruct
    fun test() {
        println("test")

        //collection.insertOne(User(null, "Nick"))
    }
}
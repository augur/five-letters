package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Repository
import javax.annotation.PostConstruct

@Repository
class UserDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<UserData>()

    fun insertNewUser(userData: UserData) {
        collection.insertOne(userData)
    }

    fun loadUserData(login: String): UserData? {
        return collection.findOne(UserData::login eq login)
    }
}
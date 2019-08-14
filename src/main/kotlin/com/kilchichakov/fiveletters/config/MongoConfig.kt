package com.kilchichakov.fiveletters.config

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MongoConfig {

    @Bean
    fun mongoDatabase(@Value("\${MONGO_LOGIN}") login: String,
                      @Value("\${MONGO_PASSWORD}") password: String,
                      @Value("\${MONGO_HOST}") host: String,
                      @Value("\${MONGO_PORT}") port: Int): MongoDatabase {
        val cred = MongoCredential.createCredential(login, "admin", password.toCharArray())
        val client = KMongo.createClient(ServerAddress(host, port), listOf(cred))
        return client.getDatabase("five-letters")
    }
}
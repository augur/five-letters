package com.kilchichakov.fiveletters.config

import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MongoConfig {

    @Bean
    fun mongoDatabase(): MongoDatabase {
        val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
        return client.getDatabase("five-letters") //normal java driver usage
    }
}
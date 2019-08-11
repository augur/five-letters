package com.kilchichakov.fiveletters.config

import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.IndexOptionDefaults
import com.mongodb.client.model.IndexOptions
import org.bson.BSON
import org.bson.BsonDocument
import org.bson.BsonValue
import org.bson.conversions.Bson
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct


@Configuration
class MongoConfig {

    @Bean
    fun mongoDatabase(): MongoDatabase {
        val client = KMongo.createClient("localhost", 27018)
        return client.getDatabase("five-letters") //normal java driver usage
    }

    @PostConstruct
    fun mongoInitialSetup() {
        if (true) return
        val db = mongoDatabase()
        val key = BsonDocument.parse("""{ "login": 1 }""")
        val indexOptions = IndexOptions().unique(true)
        db.getCollection<UserData>().createIndex(key, indexOptions)
    }

    private val INITIAL_MONGO_SETUP = """
        db.userData.createIndex( { "login": 1 }, { unique: true } )
    """.trimIndent()
}
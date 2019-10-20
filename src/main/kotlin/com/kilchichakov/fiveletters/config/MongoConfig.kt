package com.kilchichakov.fiveletters.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.KMongo
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MongoConfig {

    @Bean
    fun mongoClient(@Value("\${MONGO_LOGIN}") login: String,
                    @Value("\${MONGO_PASSWORD}") password: String,
                    @Value("\${MONGO_HOST}") host: String,
                    @Value("\${MONGO_PORT}") port: Int,
                    @Value("\${MONGO_SRV_MODE}") srvMode: Boolean): MongoClient {

        val connectionString = ConnectionString(
                if (srvMode) {
                    "mongodb+srv://$login:$password@$host/admin?retryWrites=true&w=majority"
                } else {
                    "mongodb://$login:$password@$host:$port/admin?retryWrites=true&w=majority"
                }
        )
        return KMongo.createClient(connectionString)
    }

    @Bean
    fun mongoDatabase(client: MongoClient,
                      @Value("\${MONGO_DB}") dbName: String): MongoDatabase {
        return client.getDatabase(dbName)
    }
}
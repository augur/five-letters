package com.kilchichakov.fiveletters

import com.kilchichakov.fiveletters.config.MongoConfig
import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.bson.BsonDocument


open class MongoTestSuiteExternal {

    lateinit var db: MongoDatabase

    lateinit var transactionWrapper: TransactionWrapper

    private val initScript = MongoTestSuiteExternal::class.java.classLoader.getResource("mongo-init.js").readText()

    companion object {

        private val mongoConfig = MongoConfig()

        lateinit var client: MongoClient

        @BeforeAll
        @JvmStatic
        fun setUp() {
            client = mongoConfig.mongoClient(
                  login = "fiveletters",
                    password = "111",
                    host = "111",
                    port = 27018,
                    srvMode = true
            )
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            client.close()
        }
    }

    @BeforeEach
    open fun setUpEach() {
        transactionWrapper = TransactionWrapper(client)
        db = client.getDatabase("test-db")
        val script = BasicDBObject()
        script["eval"] = initScript

        db.runCommand(BsonDocument())
    }

    @AfterEach
    fun tearDownEach() {
        client.dropDatabase("test-db")
    }

}
package com.kilchichakov.fiveletters

import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.KMongo

open class MongoTestSuite {

    lateinit var db: MongoDatabase

    private val initScript = MongoTestSuite::class.java.classLoader.getResource("mongo-init.js").readText()

    companion object {

        private lateinit var mongodExecutable: MongodExecutable
        private lateinit var client: MongoClient

        @BeforeAll
        @JvmStatic
        fun setUp() {
            val starter = MongodStarter.getDefaultInstance()

            val bindIp = "localhost"
            val port = 12345
            val mongodConfig = MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(Net(bindIp, port, Network.localhostIsIPv6()))
                    .build()

            mongodExecutable = starter.prepare(mongodConfig)
            mongodExecutable.start()

            client = KMongo.createClient(bindIp, port)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            mongodExecutable.stop()
        }
    }

    @BeforeEach
    open fun setUpEach() {
        db = client.getDatabase("test")
        val script = BasicDBObject()
        script["eval"] = initScript
        db.runCommand(script)
    }

    @AfterEach
    fun tearDownEach() {
        client.dropDatabase("test")
    }

}
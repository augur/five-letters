package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoWriteException
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.KMongo


internal class UserDataRepositoryTest {

    lateinit var repository: UserDataRepository

    val initScript = UserDataRepositoryTest::class.java.classLoader.getResource("mongo-init.js").readText()

    companion object {

        lateinit var mongodExecutable: MongodExecutable
        lateinit var client: MongoClient

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
    fun setUpEach() {
        val db = client.getDatabase("test")
        val script = BasicDBObject()
        script["eval"] = initScript
        db.runCommand(script)
        repository = UserDataRepository(db)
    }

    @AfterEach
    fun tearDownEach() {
        client.dropDatabase("test")
    }

    @Test
    fun `should insert and load users`() {
        // Given
        val login = "someLogin"
        val newUser = UserData(null, login, "pwd", "nick")

        // When
        val before = repository.loadUserData(login)
        repository.insertNewUser(newUser)
        val after = repository.loadUserData(login)

        // Then
        assertThat(before).isNull()
        assertThat(after).isEqualToIgnoringGivenFields(newUser, "_id")
        assertThat(after?._id).isNotNull()
    }

    @Test
    fun `should not permit inserting duplicate login`() {
        // Given
        val newUser = UserData(null, "someLogin", "pwd", "nick")
        repository.insertNewUser(newUser)

        // Then
        assertThrows<MongoWriteException> { repository.insertNewUser(newUser)  }
    }
}
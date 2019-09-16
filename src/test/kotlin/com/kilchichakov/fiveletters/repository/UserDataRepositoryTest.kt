package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
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


internal class UserDataRepositoryTest : MongoTestSuite() {

    private lateinit var repository: UserDataRepository

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = UserDataRepository(db)
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
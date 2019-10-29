package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoException
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
import java.lang.RuntimeException


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
        transactionWrapper.executeInTransaction {
            repository.insertNewUser(newUser, it)
        }
        val after = repository.loadUserData(login)

        // Then
        assertThat(before).isNull()
        assertThat(after).isEqualToIgnoringGivenFields(newUser, "_id")
        assertThat(after?._id).isNotNull()
    }

    @Test
    fun `should not insert due to external error`() {
        // Given
        val login = "someLogin"
        val newUser = UserData(null, login, "pwd", "nick")

        // When
        assertThrows<RuntimeException> {
            transactionWrapper.executeInTransaction {
                repository.insertNewUser(newUser, it)
                throw RuntimeException()
            }
        }
        val after = repository.loadUserData(login)

        // Then
        assertThat(after).isNull()
    }

    @Test
    fun `should not permit inserting duplicate login`() {
        // Given
        val newUser = UserData(null, "someLogin", "pwd", "nick")
        transactionWrapper.executeInTransaction {
            repository.insertNewUser(newUser, it)
        }

        // Then
        assertThrows<MongoException> {
            transactionWrapper.executeInTransaction {
                repository.insertNewUser(newUser, it)
            }
        }
    }

    @Test
    fun `should change password of user`() {
        // Given
        val login = "someLogin"
        val oldPwd = "some old pwd"
        val newPwd = "new password"
        val newUser = UserData(null, login, oldPwd, "nick")
        transactionWrapper.executeInTransaction {
            repository.insertNewUser(newUser, it)
        }

        // When
        val actual = repository.changePassword(login, newPwd)
        val updated = repository.loadUserData(login)!!

        // Then
        assertThat(actual).isTrue()
        assertThat(updated.password).isEqualTo(newPwd)
        assertThat(updated).isEqualToIgnoringGivenFields(newUser, "password", "_id")
    }
}
package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.AuthData
import com.mongodb.MongoException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.RuntimeException


internal class AuthDataRepositoryTest : MongoTestSuite() {

    private lateinit var repository: AuthDataRepository

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = AuthDataRepository(db)
    }

    @Test
    fun `should insert and load users`() {
        // Given
        val login = "someLogin"
        val newUser = AuthData(null, login, "pwd")

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
        val newUser = AuthData(null, login, "pwd")

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
        val newUser = AuthData(null, "someLogin", "pwd")
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
        val newUser = AuthData(null, login, oldPwd)
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
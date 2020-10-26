package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.FoundEmailUnconfirmed
import com.kilchichakov.fiveletters.model.FoundOk
import com.kilchichakov.fiveletters.model.NotFound
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.MongoException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
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

    @Test
    fun `should find auth data by email - success`() {
        // Given
        val email = "some@email"
        val login = "someLogin"
        val newUser = AuthData(null, login, "whatever")
        val otherUser = AuthData(null, "other login", "whatever")
        insertAuthDataWithParams(otherUser, email, false)
        insertAuthDataWithParams(newUser, email, true)

        // When
        val actual = repository.findAuthDataByEmail(email)

        // Then
        assertThat(actual).isInstanceOf(FoundOk::class.java)
        actual as FoundOk
        assertThat(actual.authData.login).isEqualTo(login)
    }

    @Test
    fun `should find auth data by email - only unconfirmed email`() {
        // Given
        val email = "some@email"
        val login = "someLogin"
        val newUser = AuthData(null, login, "whatever")
        insertAuthDataWithParams(newUser, email, false)

        // When
        val actual = repository.findAuthDataByEmail(email)

        // Then
        assertThat(actual).isInstanceOf(FoundEmailUnconfirmed::class.java)
        actual as FoundEmailUnconfirmed
        assertThat(actual.authData.login).isEqualTo(login)
    }

    @Test
    fun `should find auth data by email - not found`() {
        // Given
        val email = "some@email"
        val login = "someLogin"
        val newUser = AuthData(null, login, "whatever")
        insertAuthDataWithParams(newUser, "other@email", false)

        // When
        val actual = repository.findAuthDataByEmail(email)

        // Then
        assertThat(actual).isInstanceOf(NotFound::class.java)
        actual as NotFound
    }

    private fun insertAuthDataWithParams(authData: AuthData, email: String, emailConfirmed: Boolean) {
        transactionWrapper.executeInTransaction {
            repository.insertNewUser(authData, it)
        }
        val userDataCollection = db.getCollection("userData", UserData::class.java)
        val filter = UserData::login eq authData.login
        val update = and(setValue(UserData::email, email), setValue(UserData::emailConfirmed, emailConfirmed))
        userDataCollection.updateOne(filter, update)
    }
}
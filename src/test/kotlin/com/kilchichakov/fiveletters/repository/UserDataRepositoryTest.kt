package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

internal class UserDataRepositoryTest : MongoTestSuite() {

    private lateinit var repository: UserDataRepository

    private lateinit var collection: MongoCollection<UserData>

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = UserDataRepository(db)
        collection = db.getCollection()
    }

    @Test
    fun `should load UserData`() {
        // Given
        val login = "someLogin"
        val nick = "nick"
        val email = "nick@com"
        val userData = UserData(null, login, nick, email, true)
        collection.save(userData)

        // When
        val actual = repository.loadUserData(login)

        // Then
        assertThat(actual!!).isEqualToIgnoringGivenFields(userData, "_id")
    }

    @Test
    fun `should update all fields`() {
        // Given
        val login = "someLogin"
        val nick = "nick"
        val email = "nick@com"
        val userData = UserData(null, login, nick, email, true)
        collection.save(userData)
        val newEmail = "poupa"
        val newNickname = "loupa"

        // When
        val done = repository.updateUserData(login, newEmail, newNickname)
        val actual = repository.loadUserData(login) ?: throw Exception()

        assertThat(done).isTrue()
        assertThat(actual.email).isEqualTo(newEmail)
        assertThat(actual.nickname).isEqualTo(newNickname)
        assertThat(actual.emailConfirmed).isFalse()
    }

    @Test
    fun `should update without email`() {
        // Given
        val login = "someLogin"
        val nick = "nick"
        val email = "nick@com"
        val userData = UserData(null, login, nick, email, true)
        collection.save(userData)
        val newNickname = "loupa"

        // When
        val done = repository.updateUserData(login, email, newNickname)
        val actual = repository.loadUserData(login) ?: throw Exception()

        assertThat(done).isTrue()
        assertThat(actual.nickname).isEqualTo(newNickname)
        assertThat(actual.emailConfirmed).isTrue()
    }

    @Test
    fun `should fail if not found`() {
        // Given
        val login = "someLogin"
        val email = "nick@com"
        val newNickname = "loupa"

        // Then
        assertThrows<DatabaseException> { repository.updateUserData(login, email, newNickname) }
    }
}
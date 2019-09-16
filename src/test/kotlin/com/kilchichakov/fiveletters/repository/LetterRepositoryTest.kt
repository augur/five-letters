package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.Letter
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.id.ObjectIdGenerator
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date

internal class LetterRepositoryTest : MongoTestSuite() {

    private lateinit var repository: LetterRepository

    private lateinit var collection: MongoCollection<Letter>

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = LetterRepository(db, clock)
        collection = db.getCollection()
    }

    @Test
    fun `should be able to save letters`() {
        // Given
        val date1 = getDateTime("2017-02-16T21:00:00.000+01:00")
        val date2 = getDateTime("2018-02-16T15:00:00.000+02:35")

        val letter = Letter(null, "3", "5", true, date1, date2)

        // When
        repository.saveNewLetter(letter)

        // Then
        val found = collection.findOne()!!
        assertThat(found._id).isNotNull()
        assertThat(found).isEqualToIgnoringGivenFields(letter, "_id")
    }


    @Test
    fun `should filter by letters type`() {
        // Given
        val sendDate = getDateTime("2015-02-16T21:00:00.000+01:00")
        val login = "username"

        val expected = Letter(null, login, "123", false, sendDate, Date.from(instant))
        val tooEarly = Letter(null, login, "456", false, sendDate, Date.from(instant.plusMillis(1)))
        val alreadyRead = Letter(null, login, "789", true, sendDate, Date.from(instant))
        val anotherUser = Letter(null, "hmmm", "ABC", false, sendDate, Date.from(instant))

        // When
        repository.saveNewLetter(expected)
        repository.saveNewLetter(tooEarly)
        repository.saveNewLetter(alreadyRead)
        repository.saveNewLetter(anotherUser)
        val actual = repository.getNewLetters(login)

        // Then
        assertThat(actual.size).isEqualTo(1)
        assertThat(actual.first()).isEqualToIgnoringGivenFields(expected, "_id")
    }


    @Test
    fun `should update letter as read`() {
        // Given
        val oid = ObjectIdGenerator.generateNewId<ObjectId>().id
        val date1 = getDateTime("2017-02-16T21:00:00.000+01:00")
        val date2 = getDateTime("2018-02-16T15:00:00.000+02:35")
        val letter = Letter(oid, "login", "blah-blah", false, date1, date2)

        // When
        repository.saveNewLetter(letter)
        val actual = repository.markLetterAsRead("login", oid.toHexString())

        // Then
        val found = collection.findOneById(oid)
        assertThat(actual).isTrue()
        assertThat(found!!.read).isEqualTo(true)
        assertThat(found).isEqualToIgnoringGivenFields(letter, "read")
    }


    private fun getDateTime(s: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return format.parse(s)
    }
}
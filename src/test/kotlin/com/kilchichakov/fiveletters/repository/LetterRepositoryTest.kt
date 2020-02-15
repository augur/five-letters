package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.Letter
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
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
    fun `should filter letters ready for mailing`() {
        // Given
        val sendDate = getDateTime("2015-02-16T21:00:00.000+01:00")

        val expected = Letter(ObjectId(), "ldd", "123", false, sendDate, Date.from(instant))
        val tooEarly = Letter(ObjectId(), "3243", "456", false, sendDate, Date.from(instant.plusMillis(1)))
        val alreadyRead = Letter(ObjectId(), "fg", "789", true, sendDate, Date.from(instant))
        val alreadyMailed = Letter(ObjectId(), "3243", "789", false, sendDate, Date.from(instant), true)

        // When
        repository.run {
            saveNewLetter(expected)
            saveNewLetter(tooEarly)
            saveNewLetter(alreadyRead)
            saveNewLetter(alreadyMailed)
        }
        val actual = repository.getLettersForMailing()

        // Then
        assertThat(actual).containsExactly(expected)
    }

    @Test
    fun `should get future letters`() {
        // Given
        val sendDate1 = getDateTime("2015-02-16T21:00:00.000+01:00")
        val sendDate2 = getDateTime("2015-02-17T21:00:00.000+01:00")
        val sendDate3 = getDateTime("2015-02-18T21:00:00.000+01:00")
        val sendDate4 = getDateTime("2015-02-19T21:00:00.000+01:00")
        val sendDate5 = getDateTime("2015-02-20T21:00:00.000+01:00")
        val login = "username"

        val expectedLast = Letter(null, login, "123", false, sendDate1, Date.from(instant.plusMillis(500)))
        val expectedFirst = Letter(null, login, "123", false, sendDate2, Date.from(instant.plusMillis(100)))
        val limitedOut = Letter(null, login, "123", false, sendDate3, Date.from(instant.plusMillis(800)))
        val anotherUser = Letter(null, "hmmm", "123", false, sendDate4, Date.from(instant.plusMillis(100)))
        val notInFuture = Letter(null, login, "123", false, sendDate5, Date.from(instant.plusMillis(-100)))

        // When
        repository.saveNewLetter(expectedLast)
        repository.saveNewLetter(expectedFirst)
        repository.saveNewLetter(limitedOut)
        repository.saveNewLetter(anotherUser)
        repository.saveNewLetter(notInFuture)
        val actual = repository.getFutureLetters(login, 2)

        // Then
        assertThat(actual.map { it.sendDate }.toList()).containsExactly(expectedFirst.sendDate, expectedLast.sendDate)
    }


    @Test
    fun `should update letter as read`() {
        // Given
        val oid = ObjectId()
        val date1 = getDateTime("2017-02-16T21:00:00.000+01:00")
        val date2 = getDateTime("2018-02-16T15:00:00.000+02:35")
        val letter = Letter(oid, "login", "blah-blah", false, date1, date2)

        // When
        repository.saveNewLetter(letter)
        val actual = repository.markLetterAsRead("login", oid.toString())

        // Then
        val found = collection.findOneById(oid)
        assertThat(actual).isTrue()
        assertThat(found!!.read).isEqualTo(true)
        assertThat(found).isEqualToIgnoringGivenFields(letter, "read")
    }

    @Test
    fun `should update letters as mail sent`() {
        // Given
        val sendDate = getDateTime("2015-02-16T21:00:00.000+01:00")

        val letter = Letter(ObjectId(), "ldd", "123", false, sendDate, Date.from(instant))
        val id = letter._id.toString()

        // When
        repository.saveNewLetter(letter)
        val actual = repository.markLettersAsMailed(listOf(id))

        // Then
        val found = collection.findOneById(letter._id!!) ?: throw Exception()
        assertThat(actual).isTrue()
        assertThat(found.mailSent).isEqualTo(true)
        assertThat(found).isEqualToIgnoringGivenFields(letter, "mailSent")
    }

    @Test
    fun `should perform complex inbox query`() {
        // Given
        val login = "loupa"
        val skip = 1
        val limit = 3

        val three_second_past = Date.from(instant.minusMillis(3000))
        val two_second_past = Date.from(instant.minusMillis(2000))
        val one_second_past = Date.from(instant.minusMillis(1000))
        val one_second_future = Date.from(instant.plusMillis(1000))
        val two_second_future = Date.from(instant.plusMillis(2000))
        val three_second_future = Date.from(instant.plusMillis(3000))

        val outLimit = Letter(ObjectId(), login, "4", false, three_second_future, three_second_past, false)
        val expectedThird = Letter(ObjectId(), login, "3333", false, two_second_past, two_second_past, false)
        val expectedSecond = Letter(ObjectId(), login, "222", false, one_second_past, two_second_past, false)
        val expectedFirst = Letter(ObjectId(), login, "11", false, one_second_future, one_second_past, false)
        val skipped = Letter(ObjectId(), login, "0", false, two_second_future, one_second_past, false)
        val anotherLogin = Letter(ObjectId(), "poupa", "5", false, Date.from(instant.plusMillis(100)), Date.from(instant), false)
        val read = Letter(ObjectId(), login, "6", true, Date.from(instant.plusMillis(100)), Date.from(instant), false)
        val mailed = Letter(ObjectId(), login, "7", false, Date.from(instant.plusMillis(100)), Date.from(instant), true)
        val archived = Letter(ObjectId(), login, "9", false, Date.from(instant.plusMillis(100)), Date.from(instant), false, true)
        val notReady = Letter(ObjectId(), login, "8", false, Date.from(instant.plusMillis(100)), one_second_future, false)

        listOf(expectedThird, expectedFirst, expectedSecond, skipped, outLimit, anotherLogin, read, mailed, archived, notReady)
                .forEach(repository::saveNewLetter)

        val testCases = listOf(
                TestCase(
                        action = { repository.inbox(login, skip, limit, includeRead = false, includeMailed = false, includeArchived = false) },
                        assert = { assertThat(it.elements).containsExactly(expectedFirst, expectedSecond, expectedThird) }
                ),
                TestCase(
                        action = { repository.inbox(login, skip, limit, includeRead = false, includeMailed = false, includeArchived = false, sortBy = "sendDate") },
                        assert = { assertThat(it.elements).containsExactly(skipped, expectedFirst, expectedSecond) }
                )
        )

        // When
        testCases.forEach {
            val actual = it.action()
            //Then
            it.assert(actual)
            assertThat(actual.pageNumber).isEqualTo(1)
            assertThat(actual.pageSize).isEqualTo(3)
            assertThat(actual.total).isEqualTo(5)
        }
    }

    data class TestCase<ACTUAL>(
            val action: () -> ACTUAL,
            val assert: (ACTUAL) -> Unit
    )

    private fun getDateTime(s: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return format.parse(s)
    }
}
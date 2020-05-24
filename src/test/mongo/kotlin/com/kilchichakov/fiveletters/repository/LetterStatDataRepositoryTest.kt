package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.model.LetterStatData
import com.kilchichakov.fiveletters.model.UserData
import com.mongodb.client.MongoCollection
import dev.ktobe.toBe
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.litote.kmongo.getCollection
import kotlin.concurrent.thread

internal class LetterStatDataRepositoryTest : MongoTestSuite() {

    private lateinit var repository: LetterStatDataRepository

    private lateinit var collection: MongoCollection<LetterStatData>

    val login = "loupa"
    val april10Stat = LetterStat(Day(2020, 4, 10), 2)
    val april1Stat = LetterStat(Day(2020, 4, 1), 3)
    val may1Stat = LetterStat(Day(2020, 5, 1), 3)
    val april15Stat = LetterStat(Day(2020, 4, 15), 2)

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = LetterStatDataRepository(db)
        collection = db.getCollection<LetterStatData>("userData")
    }

    @Test
    fun `should be able to sorted set and get stat data`() {
        // Given
        val day1 = Day(2020, 1, 1)
        val day2 = Day(2020, 1, 2)
        collection.insertOne(LetterStatData(null, login, emptyList(), emptyList(), listOf(day1), listOf(day2)))
        val sentStats = listOf(april10Stat, april1Stat)
        val openStats = listOf(may1Stat, april15Stat)

        // When
        transactionWrapper.executeInTransaction {
            val set = repository.setStatData(login, sentStats, openStats)
            val actual = repository.getStatData(login)!!

            // Then
            assertThat(set).isTrue()
            assertThat(actual.login).isEqualTo(login)
            assertThat(actual.sentStat).containsExactly(april1Stat, april10Stat)
            assertThat(actual.openStat).containsExactly(april15Stat, may1Stat)
            assertThat(actual.unorderedSent).isEmpty()
            assertThat(actual.unorderedOpen).isEmpty()
        }
    }

    @Test
    fun `should not update anything if record not found`() {
        // Given
        val sentStats = listOf(april10Stat, april1Stat)
        val openStats = listOf(may1Stat, april15Stat)

        // When
        transactionWrapper.executeInTransaction {
            val set = repository.setStatData(login, sentStats, openStats)

            // The
            assertThat(set).isFalse()
        }
    }

    @Test
    fun `should throw if setData is invalid`() {
        // Given
        val sentStats = listOf(april1Stat, april1Stat)
        val openStats = listOf(may1Stat, may1Stat)

        // Then
        assertThatCode {
            repository.setStatData(login, sentStats, openStats)
        }.isInstanceOf(DataException::class.java)
    }

    @Test
    fun `should add stats`() {
        // Given
        val day1 = Day(2020, 1, 1)
        val day2 = Day(2020, 1, 2)
        collection.insertOne(LetterStatData(null, login,
                listOf(april1Stat, april10Stat),
                listOf(april15Stat, may1Stat),
                listOf(day1),
                listOf(day2))
        )
        val april5Day = Day(2020, 4, 5)
        val april15Day = Day(2020, 4, 15)

        // When
        repository.addStat(login, sent = april5Day , open = april15Day)
        val actual = repository.getStatData(login)!!

        // Then
        assertThat(actual.login).isEqualTo(login)
        assertThat(actual.sentStat).containsExactly(april1Stat, april10Stat)
        assertThat(actual.openStat).containsExactly(april15Stat, may1Stat)
        assertThat(actual.unorderedSent).containsExactly(day1, april5Day)
        assertThat(actual.unorderedOpen).containsExactly(day2, april15Day)
    }

    @Test
    fun `should not lose data during multiple transaction calls`() {
        // Given
        val day1 = Day(2020, 1, 1)
        val day2 = Day(2020, 1, 2)
        val day3 = Day(2020, 1, 3)
        val day4 = Day(2020, 1, 4)
        collection.insertOne(LetterStatData(null, login,
                listOf(april1Stat),
                listOf(april15Stat),
                listOf(day1),
                listOf(day2))
        )
        var t1failed = false
        var t2failed = false

        val t1 = thread(start = false) {
            try {
                transactionWrapper.executeInTransaction {
                    val stats = repository.getStatData(login)!!
                    Thread.sleep(1000)
                    repository.setStatData(login, listOf(april1Stat, april10Stat), listOf(april15Stat, may1Stat))
                }
            } catch (e: Exception) {
                t1failed = true
            }
        }
        val t2 = thread(start = false) {
            try {
                transactionWrapper.executeInTransaction {
                    Thread.sleep(500)
                    repository.addStat(login, day3, day4)
                }
            } catch (e: Exception) {
                t2failed = true
            }
        }

        // When
        t1.start()
        t2.start()
        t1.join()
        t2.join()
        val actual = repository.getStatData(login)!!

        // Then
        t1failed toBe true
        t2failed toBe false
        assertThat(actual.login).isEqualTo(login)
        assertThat(actual.sentStat).containsExactly(april1Stat)
        assertThat(actual.openStat).containsExactly(april15Stat)
        assertThat(actual.unorderedSent).containsExactlyInAnyOrder(day1, day3)
        assertThat(actual.unorderedOpen).containsExactlyInAnyOrder(day2, day4)
    }

    @Test
    fun `should get iterable for users with unordered stats`() {
        // Given
        val day1 = Day(2020, 1, 1)
        val day2 = Day(2020, 1, 2)
        val day3 = Day(2020, 1, 3)
        val day4 = Day(2020, 1, 4)
        val withSent = "loupa"
        val withOpen = "poupa"
        val withBoth = "loupeaux"
        val without = "poupon"
        collection.insertOne(LetterStatData(null, withSent, emptyList(), emptyList(), listOf(day1), listOf()))
        collection.insertOne(LetterStatData(null, withOpen, emptyList(), emptyList(), listOf(), listOf(day2)))
        collection.insertOne(LetterStatData(null, withBoth, emptyList(), emptyList(), listOf(day3), listOf(day4)))
        collection.insertOne(LetterStatData(null, without, emptyList(), emptyList(), listOf(), listOf()))

        // When
        val actual = ArrayList<String>()
        repository.iterateLoginsWithUnorderedStats().forEach {
            actual.add(it)
        }

        // Then
        assertThat(actual).containsExactlyInAnyOrder(withSent, withOpen, withBoth)
    }
}
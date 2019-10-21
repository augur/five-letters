package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.TimePeriod
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.getCollection
import org.litote.kmongo.save

internal class TimePeriodRepositoryTest : MongoTestSuite() {

    private lateinit var repository: TimePeriodRepository

    private lateinit var collection: MongoCollection<TimePeriod>

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = TimePeriodRepository(db)
        collection = db.getCollection()
    }

    @Test
    fun `should get timePeriod`() {
        // Given
        val name = "somePeriod"
        val timePeriod = TimePeriod(name, 1, 2, 3, 4, true)
        collection.save(timePeriod)

        // When
        val actual = repository.getTimePeriod(name)

        // Then
        assertThat(actual).isEqualTo(timePeriod)
    }

    @Test
    fun `should unable to get disabled timePeriod`() {
        // Given
        val name = "somePeriod"
        val timePeriod = TimePeriod(name, 1, 2, 3, 4, false)
        collection.save(timePeriod)

        // When
        assertThrows<DataException> {
            repository.getTimePeriod(name)
        }
    }

    @Test
    fun `should return ordered time periods list`() {
        // Given
        val most = TimePeriod("most", 5, 5, 5, 5, true)
        val mostDisabled = TimePeriod("mostDisabled", 0, 0, 0, 2, false)
        val least = TimePeriod("least", 2, 0, 0, 0, true)
        val leastDisabled = TimePeriod("leastDisabled", 10, 0, 0, 0, false)
        val almostMost = TimePeriod("almostMost", 4, 4, 4, 0, true)
        val almostMostDisabled = TimePeriod("almostMostDisabled", 0, 0, 3, 0, false)
        val almostLeast = TimePeriod("almostLeast", 3, 3, 0, 0, true)
        val almostLeastDisabled = TimePeriod("almostLeastDisabled", 0, 4, 0, 0, false)
        collection.save(most)
        collection.save(mostDisabled)
        collection.save(least)
        collection.save(leastDisabled)
        collection.save(almostMost)
        collection.save(almostMostDisabled)
        collection.save(almostLeast)
        collection.save(almostLeastDisabled)

        val expected = listOf("least", "almostLeast", "almostMost", "most")

        // When
        val actual = repository.listTimePeriods()

        // Then
        assertThat(actual).isEqualTo(expected)
    }
}
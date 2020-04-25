package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.model.DEFAULT_STATE
import com.kilchichakov.fiveletters.model.SystemState
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.litote.kmongo.getCollection

internal class SystemStateRepositoryTest : MongoTestSuite() {

    private lateinit var repository: SystemStateRepository

    private lateinit var collection: MongoCollection<SystemState>

    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        repository = SystemStateRepository(db)
        collection = db.getCollection()
    }

    @Test
    fun `should return default state and insert it`() {
        // When
        val actual = repository.read()

        // Then
        assertThat(actual).isEqualTo(DEFAULT_STATE)
        assertThat(collection.countDocuments()).isEqualTo(1L)
    }

    @Test
    fun `should read saved state`() {
        // Given
        val expected = SystemState(true)
        collection.insertOne(expected)

        // When
        val actual = repository.read()

        // Then
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `should remain only single state`() {
        // Given
        val one = SystemState(true)
        val two = SystemState(false)
        collection.insertOne(one)
        collection.insertOne(two)

        // When
        val count = collection.countDocuments()

        // Then
        assertThat(count).isEqualTo(1L)
    }

    @Test
    fun `should switch registration`() {
        // When
        val initial = repository.read()
        repository.switchRegistration(true)
        val enabled = repository.read()
        repository.switchRegistration(false)
        val disabled = repository.read()

        // Then
        assertThat(initial.registrationEnabled).isFalse()
        assertThat(enabled.registrationEnabled).isTrue()
        assertThat(disabled.registrationEnabled).isFalse()
    }
}
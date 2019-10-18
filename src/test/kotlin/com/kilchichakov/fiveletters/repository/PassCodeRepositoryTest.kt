package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.MongoTestSuite
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.OneTimePassCodeConsumed
import com.kilchichakov.fiveletters.model.PassCode
import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.kilchichakov.fiveletters.util.now
import com.mongodb.MongoException
import com.mongodb.client.MongoCollection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import java.lang.Exception
import java.lang.RuntimeException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

internal class PassCodeRepositoryTest : MongoTestSuite() {

    private lateinit var repository: PassCodeRepository

    private lateinit var collection: MongoCollection<PassCode>

    private lateinit var transactionWrapper: TransactionWrapper

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))


    @BeforeEach
    override fun setUpEach() {
        super.setUpEach()
        transactionWrapper = TransactionWrapper(client)
        repository = PassCodeRepository(db, clock)
        collection = db.getCollection()
    }

    @Test
    fun `should insert new passCode`() {
        // Given
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)

        // When
        repository.insertPassCode(passCode)

        // Then
        assertThat(collection.findOneById(code)).isEqualTo(passCode)
    }

    @Test
    fun `should fail to insert passCode twice`() {
        // Given
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)

        // When
        repository.insertPassCode(passCode)
        assertThrows<MongoException> { repository.insertPassCode(passCode) }
    }

    @Test
    fun `should find passCode`() {
        // Given
        val code = "oh-wow"
        val date = Date()
        val expected = OneTimePassCode(code, date)
        collection.save(expected)

        // When
        val actual = repository.findPassCode(code)

        // Then
        assertThat(actual!!).isEqualTo(expected)
    }

    @Test
    fun `should successfully update passCode`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)
        collection.save(passCode)

        // When
        transactionWrapper.executeInTransaction {
            repository.consumeOneTimePassCode(code, login, it)
        }
        val actual = repository.findPassCode(code)

        // Then
        actual as OneTimePassCodeConsumed
        assertThat(actual._id).isEqualTo(passCode._id)
        assertThat(actual.date).isEqualTo(Date.from(instant))
        assertThat(actual.login).isEqualTo(login)
    }

    @Test
    fun `should fail to update due external error`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)
        collection.save(passCode)

        // When
        assertThrows<RuntimeException> {
            transactionWrapper.executeInTransaction {
                repository.consumeOneTimePassCode(code, login, it)
                throw RuntimeException()
            }
        }

        val actual = repository.findPassCode(code)
        assertThat(actual!!).isEqualTo(passCode)
    }

    @Test
    fun `should fail to update if passcode is overdue`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date.from(Instant.ofEpochMilli(1468458309619))
        val passCode = OneTimePassCode(code, date)
        collection.save(passCode)

        // When
        assertThrows<DataException> {
            transactionWrapper.executeInTransaction {
                repository.consumeOneTimePassCode(code, login, it)
            }
        }

        val actual = repository.findPassCode(code)
        assertThat(actual!!).isEqualTo(passCode)
    }

    @Test
    fun `should fail to update if passcode is not one-time passcode`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date.from(Instant.ofEpochMilli(1468458309619))
        val passCode = OneTimePassCodeConsumed(code, "poupa", date)
        collection.save(passCode)

        // When
        assertThrows<DataException> {
            transactionWrapper.executeInTransaction {
                repository.consumeOneTimePassCode(code, login, it)
            }
        }

        val actual = repository.findPassCode(code)
        assertThat(actual!!).isEqualTo(passCode)
    }

    @Test
    fun `should fail to update if passcode not found`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)

        // When
        assertThrows<DatabaseException> {
            transactionWrapper.executeInTransaction {
                repository.consumeOneTimePassCode(code, login, it)
            }
        }
    }

    @Test
    fun `only one of threads should successfully update passCode`() {
        // Given
        val login = "loupa"
        val code = "oh-wow"
        val date = Date()
        val passCode = OneTimePassCode(code, date)
        collection.save(passCode)

        val wins = AtomicInteger(0)
        val fails = AtomicInteger(0)
        val done = CountDownLatch(32)

        val exec = {
            try {
                transactionWrapper.executeInTransaction {
                    repository.consumeOneTimePassCode(code, login, it)
                    wins.incrementAndGet()
                }
            } catch (e: Exception) {
                fails.incrementAndGet()
            } finally {
                done.countDown()
            }
        }
        val threads = (1..32).asSequence().map {
            Thread { exec() }
        }

        // When
        threads.forEach { it.start() }

        // Then
        done.await()
        assertThat(wins.get()).isEqualTo(1)
        assertThat(fails.get()).isEqualTo(31)
        val actual = repository.findPassCode(code)
        actual as OneTimePassCodeConsumed
        assertThat(actual._id).isEqualTo(passCode._id)
        assertThat(actual.date).isEqualTo(Date.from(instant))
        assertThat(actual.login).isEqualTo(login)
    }
}
package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.mongodb.MongoClient
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.bson.conversions.Bson
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.provider.Arguments
import org.litote.kmongo.findOne

@ExtendWith(MockKExtension::class)
internal class TransactionWrapperTest {

    @RelaxedMockK
    lateinit var client: MongoClient

    @InjectMockKs
    lateinit var wrapper: TransactionWrapper

    @Test
    fun `should execute in transaction`() {
        // Given
        val session = mockk<ClientSession>(relaxed = true)
        val block = mockk<(ClientSession) -> Unit>(relaxed = true)

        every { client.startSession() } returns session

        // When
        wrapper.executeInTransaction(block)

        // Then
        verify {
            client.startSession()
            session.startTransaction()
            block.invoke(session)
            session.commitTransaction()
            session.close()
        }

        confirmVerified(client, session, block)
    }

    @Test
    fun `should abort transaction on failure`() {
        // Given
        val session = mockk<ClientSession>(relaxed = true)
        val block = mockk<(ClientSession) -> Unit>(relaxed = true)

        every { client.startSession() } returns session
        every { block.invoke(any()) } throws IllegalThreadStateException()

        // When
        assertThrows<IllegalThreadStateException> { wrapper.executeInTransaction(block) }

        // Then
        verify {
            client.startSession()
            session.startTransaction()
            block.invoke(session)
            session.abortTransaction()
            session.close()
        }

        confirmVerified(client, session, block)
    }

    @Nested
    inner class TransactionalMethodsTest {

        lateinit var session: ClientSession
        lateinit var collection: MongoCollection<Int>
        lateinit var filter: Bson
        lateinit var update: Bson
        var document: Int = 42

        @BeforeEach
        fun setUp() {
            session = mockk(relaxed = true)
            collection = mockk(relaxed = true)
            filter = mockk()
            update = mockk()
            every { client.startSession() } returns session
        }

        fun methods() = listOf(
                Args(
                        { mandatory -> collection.updateOneInTransaction(filter, update, mandatory) },
                        { collection.updateOne(session, filter, update) },
                        { collection.updateOne(filter, update) }
                ),
                Args(
                        { mandatory -> collection.findOneInTransaction(filter, mandatory) },
                        { collection.findOne(session, filter) },
                        { collection.findOne(filter) }
                ),
                Args(
                        { mandatory -> collection.insertOneInTransaction(document, mandatory) },
                        { collection.insertOne(session, document) },
                        { collection.insertOne(document) }
                )
        )

        @Test
        fun `should run method in transaction`() {
            methods().forEach { args ->
                // When
                wrapper.executeInTransaction {
                    args.methodCall(true)
                }

                // Then
                verify { args.collectionCall() }
                confirmVerified(collection)
            }
        }

        @Test
        fun `should fail to run if transaction is mandatory and not exist`() {
            methods().forEach { args ->
                // When
                assertThatCode {
                    args.methodCall(true)
                }.isInstanceOf(DatabaseException::class.java)

                // Then
                confirmVerified(collection)
            }
        }

        @Test
        fun `should not fail if transaction is not mandatory and not exist`() {
            methods().forEach { args ->
                // When
                assertThatCode {
                    args.methodCall(false)
                }.doesNotThrowAnyException()

                // Then
                verify { args.collectionCallNoSession() }
                confirmVerified(collection)
            }
        }
    }

    data class Args(val methodCall: (Boolean) -> Unit,
                    val collectionCall: () -> Unit,
                    val collectionCallNoSession: () -> Unit)
}
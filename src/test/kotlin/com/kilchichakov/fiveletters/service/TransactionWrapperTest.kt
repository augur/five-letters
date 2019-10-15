package com.kilchichakov.fiveletters.service

import com.mongodb.MongoClient
import com.mongodb.client.ClientSession
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

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
}
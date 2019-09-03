package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.repository.LetterRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class LetterServiceTest {

    @RelaxedMockK
    lateinit var letterRepository: LetterRepository

    @InjectMockKs
    lateinit var service: LetterService


    @Test
    fun `should successfully mark letter as read`() {
        // Given
        val login = "someLogin"
        val id = "someId"
        every { letterRepository.markLetterAsRead(any(), any()) } returns true

        // When
        assertDoesNotThrow { service.markLetterAsRead(login, id) }

        // Then
        verify { letterRepository.markLetterAsRead(login, id) }
        confirmVerified(letterRepository)
    }

    @Test
    fun `should throw on unsuccessful mark letter as read`() {
        // Given
        val login = "someLogin"
        val id = "someId"
        every { letterRepository.markLetterAsRead(any(), any()) } returns false

        // When
        assertThrows<DatabaseException> { service.markLetterAsRead(login, id)  }


        // Then
        verify { letterRepository.markLetterAsRead(login, id) }
        confirmVerified(letterRepository)
    }
}
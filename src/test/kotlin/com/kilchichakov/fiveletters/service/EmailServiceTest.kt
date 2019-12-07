package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.repository.UserDataRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class EmailServiceTest {

    @RelaxedMockK
    lateinit var userDataRepository: UserDataRepository

    @InjectMockKs
    lateinit var service: EmailService

    @Test
    fun `should confirm email by code`() {
        // Given
        val code = "some code"
        every { userDataRepository.setEmailConfirmed(any()) } returns true

        // When
        service.confirmEmailByCode(code)

        // Then
        verify {
            userDataRepository.setEmailConfirmed(code)
        }
        confirmVerified(userDataRepository)
    }

    @Test
    fun `should throw in case of email not confirmed`() {
        // Given
        val code = "some code"
        every { userDataRepository.setEmailConfirmed(any()) } returns false
        // When
        assertThrows<DatabaseException> { service.confirmEmailByCode(code) }

        // Then
        verify {
            userDataRepository.setEmailConfirmed(code)
        }
        confirmVerified(userDataRepository)
    }
}
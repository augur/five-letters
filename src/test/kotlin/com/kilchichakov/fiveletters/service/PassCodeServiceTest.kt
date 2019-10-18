package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.PassCode
import com.kilchichakov.fiveletters.repository.PassCodeRepository
import com.mongodb.client.ClientSession
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class PassCodeServiceTest {

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)
    private val clock: Clock = Clock.fixed(instant, ZoneId.of("Indian/Maldives"))

    @RelaxedMockK
    lateinit var passCodeRepository: PassCodeRepository

    lateinit var service: PassCodeService

    @BeforeEach
    fun setUp() {
        service = PassCodeService(clock, passCodeRepository)
    }

    @Test
    fun `should get passcode from repo`() {
        // Given
        val code = "someCode"
        val passCode = mockk<PassCode>()
        every { passCodeRepository.findPassCode(any()) } returns passCode

        // When
        val actual = service.getPassCode(code)

        // Then
        assertThat(actual).isEqualTo(passCode)
        verify { passCodeRepository.findPassCode(code) }
        confirmVerified(passCodeRepository)
    }

    @Test
    fun `should throw if no passcode found in repo`() {
        // Given
        val code = "someCode"
        every { passCodeRepository.findPassCode(any()) } returns null

        // When
        assertThrows<DataException> { service.getPassCode(code)  }

        // Then
        verify { passCodeRepository.findPassCode(code) }
        confirmVerified(passCodeRepository)
    }

    @Test
    fun `should generate one-time passcodes`() {
        // Given
        val seconds = 100500L
        val valid = Instant.ofEpochMilli(instant.toEpochMilli() + seconds * 1000)

        // When
        val actual1 = service.generateOneTimePassCode(seconds)
        val actual2 = service.generateOneTimePassCode(seconds)

        // Then
        assertThat(actual1.validUntil).isEqualTo(Date.from(valid))
        assertThat(actual2.validUntil).isEqualTo(Date.from(valid))
        assertThat(actual1._id).isNotEqualTo(actual2._id)
        verify {
            passCodeRepository.insertPassCode(actual1)
            passCodeRepository.insertPassCode(actual2)
        }
        confirmVerified(passCodeRepository)
    }

    @Test
    fun `should consume one-time passcode`() {
        // Given
        val passCode = mockk<OneTimePassCode>()
        val login = "loupa"
        val session = mockk<ClientSession>()
        val code = "someCode"

        every { passCode._id } returns code

        // When
        service.usePassCode(passCode, login, session)

        // Then
        verify {
            passCodeRepository.consumeOneTimePassCode(code, login, session)
        }
        confirmVerified(passCodeRepository)
    }
}
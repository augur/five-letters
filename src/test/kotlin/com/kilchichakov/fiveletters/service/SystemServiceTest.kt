package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.repository.SystemStateRepository
import io.mockk.confirmVerified
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SystemServiceTest {

    @RelaxedMockK
    lateinit var systemStateRepository: SystemStateRepository

    @InjectMockKs
    lateinit var service: SystemService

    @Test
    fun `should switch registration`() {
        // Given
        val enabled = true

        // When
        service.switchRegistration(enabled)

        // Then
        verify { systemStateRepository.switchRegistration(enabled) }
        confirmVerified(systemStateRepository)
    }
}
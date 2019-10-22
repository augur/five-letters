package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.TimePeriod
import com.kilchichakov.fiveletters.repository.TimePeriodRepository
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TimePeriodServiceTest {

    @RelaxedMockK
    lateinit var timePeriodRepository: TimePeriodRepository

    @InjectMockKs
    lateinit var service: TimePeriodService

    @Test
    fun `should get time period by name`() {
        // Given
        val name = "someName"
        val period = mockk<TimePeriod>()

        every { timePeriodRepository.getTimePeriod(any()) } returns period

        // When
        val actual = service.getTimePeriod(name)

        // Then
        assertThat(actual).isEqualTo(period)
        verify {
            timePeriodRepository.getTimePeriod(name)
        }
        confirmVerified(timePeriodRepository)
    }

    @Test
    fun `should list all time periods`() {
        // Given
        val expected = listOf("some String")
        every { timePeriodRepository.listTimePeriods() } returns expected

        // When
        val actual = service.listAllTimePeriods()

        // Then
        assertThat(actual).isEqualTo(expected)
    }
}
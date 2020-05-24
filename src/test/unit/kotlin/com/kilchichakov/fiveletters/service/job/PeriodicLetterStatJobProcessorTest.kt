package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.model.job.PeriodicLetterStatJobPayload
import com.kilchichakov.fiveletters.service.LetterStatDataService
import io.mockk.Ordering
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class PeriodicLetterStatJobProcessorTest {

    @RelaxedMockK
    lateinit var letterStatDataService: LetterStatDataService

    @InjectMockKs
    lateinit var processor: PeriodicLetterStatJobProcessor

    @Test
    fun `should execute job`() {
        // Given
        val payload = mockk<PeriodicLetterStatJobPayload>()
        val login1 = "loupa"
        val login2 = "poupa"
        every { letterStatDataService.getLoginsWithUnorderedStatsSequence() } returns sequenceOf(login1, login2)

        // When
        processor.process(payload)

        // Then
        verify(ordering = Ordering.ORDERED) {
            letterStatDataService.getLoginsWithUnorderedStatsSequence()
            letterStatDataService.orderStats(login1)
            letterStatDataService.orderStats(login2)
        }
        confirmVerified(letterStatDataService)
    }
}
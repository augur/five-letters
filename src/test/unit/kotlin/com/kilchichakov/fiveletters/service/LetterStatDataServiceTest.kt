package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.getDateTime
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.repository.LetterStatDataRepository
import com.kilchichakov.fiveletters.setUpTransactionWrapperMock
import io.mockk.Ordering
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class LetterStatDataServiceTest {

    @RelaxedMockK
    lateinit var transactionWrapper: TransactionWrapper

    @RelaxedMockK
    lateinit var letterStatDataRepository: LetterStatDataRepository

    @RelaxedMockK
    lateinit var letterService: LetterService

    @InjectMockKs
    lateinit var service: LetterStatDataService

    val LOGIN = "loupa"

    @Test
    fun `should recalculate letter stat data`() {
        // Given
        val letter1 = SealedLetterEnvelop(getDateTime("2020-04-1T12:00:00.000+01:00"), getDateTime("2020-05-10T12:00:00.000+01:00"))
        val letter2 = SealedLetterEnvelop(getDateTime("2020-04-10T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        val letter3 = SealedLetterEnvelop(getDateTime("2020-04-10T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        val letter4 = SealedLetterEnvelop(getDateTime("2020-04-15T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        every { letterService.getLettersDatesSequence(any()) } returns sequenceOf(letter1, letter2, letter3, letter4)
        val expectedSent = arrayOf(
                LetterStat(Day(2020, 4, 1), 1),
                LetterStat(Day(2020, 4, 10), 2),
                LetterStat(Day(2020, 4, 15), 1)
        )
        val expectedOpen = arrayOf(
                LetterStat(Day(2020, 5, 10), 1),
                LetterStat(Day(2020, 5, 15), 3)
        )
        val sentSlot = slot<List<LetterStat>>()
        val openSlot = slot<List<LetterStat>>()

        // When
        service.recalculateStatData(LOGIN)

        // Then
        verify(ordering = Ordering.ORDERED) {
            letterService.getLettersDatesSequence(LOGIN)
            letterStatDataRepository.setStatData(LOGIN, capture(sentSlot), capture(openSlot))
        }
        confirmVerified(letterService, letterStatDataRepository)
        assertThat(sentSlot.captured).containsExactlyInAnyOrder(*expectedSent)
        assertThat(openSlot.captured).containsExactlyInAnyOrder(*expectedOpen)
    }
}
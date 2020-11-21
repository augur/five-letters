package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.model.LetterStatData
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.dto.GetLetterStatResponse
import com.kilchichakov.fiveletters.repository.LetterStatDataRepository
import com.kilchichakov.fiveletters.setUpTransactionWrapperMock
import com.kilchichakov.fiveletters.util.getDateTime
import com.mongodb.client.MongoCursor
import dev.ktobe.toBe
import dev.ktobe.toBeEqual
import dev.ktobe.toContainExactly
import io.mockk.Ordering
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
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

    @RelaxedMockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var service: LetterStatDataService

    val LOGIN = "loupa"

    @Test
    fun `should get letter stat data for login`() {
        // Given
        val mockSent = listOf(mockk<LetterStat>())
        val mockOpen = listOf(mockk<LetterStat>())
        val statData = mockk<LetterStatData> {
            every { sentStat } returns mockSent
            every { openStat } returns mockOpen
            every { unorderedSent } returns emptyList()
            every { unorderedOpen } returns emptyList()
        }
        every { letterStatDataRepository.getStatData(any()) } returns statData

        // When
        val actual = service.getLetterStats(LOGIN)

        // Then
        actual toBeEqual GetLetterStatResponse(mockSent, mockOpen)
        verify {
            letterStatDataRepository.getStatData(LOGIN)
        }
        confirmVerified(letterStatDataRepository)
    }

    @Test
    fun `should throw if no stat data found`() {
        // Given
        every { letterStatDataRepository.getStatData(any()) } returns null

        // Then
        assertThatCode {
            service.getLetterStats(LOGIN)
        }.isInstanceOf(DatabaseException::class.java)
    }

    @Test
    fun `should recalculate letter stat data`() {
        // Given
        val letter1 = SealedLetterEnvelop(getDateTime("2020-04-1T12:00:00.000+01:00"), getDateTime("2020-05-10T12:00:00.000+01:00"))
        val letter2 = SealedLetterEnvelop(getDateTime("2020-04-10T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        val letter3 = SealedLetterEnvelop(getDateTime("2020-04-10T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        val letter4 = SealedLetterEnvelop(getDateTime("2020-04-15T12:00:00.000+01:00"), getDateTime("2020-05-15T12:00:00.000+01:00"))
        every { letterService.getLettersDatesSequence(any()) } returns sequenceOf(letter1, letter2, letter3, letter4)
        every { userService.loadUserData(any()).timeZone } returns "Africa/Tunis"
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
            userService.loadUserData(LOGIN)
            letterService.getLettersDatesSequence(LOGIN)
            letterStatDataRepository.setStatData(LOGIN, capture(sentSlot), capture(openSlot))
        }
        confirmVerified(letterService, letterStatDataRepository)
        assertThat(sentSlot.captured).containsExactlyInAnyOrder(*expectedSent)
        assertThat(openSlot.captured).containsExactlyInAnyOrder(*expectedOpen)
    }

    @Test
    fun `should add letter stats`() {
        // Given
        val sent = getDateTime("2017-02-16T23:00:00.000+00:00")
        val open = getDateTime("2018-12-03T21:00:00.000+00:00")
        val timeZone = "Europe/Moscow"
        val letter = mockk<Letter> {
            every { login } returns LOGIN
            every { sendDate } returns sent
            every { openDate } returns open
        }
        val expectedSent = Day(2017, 2, 17)
        val expectedOpen = Day(2018, 12, 4)

        // When
        service.addLetterStats(letter, timeZone)

        // Then
        verify {
            letterStatDataRepository.addStat(LOGIN, expectedSent, expectedOpen)
        }
        confirmVerified(letterStatDataRepository)
    }

    @Test
    fun `should get a sequence of logins with unordered stats`() {
        // Given
        val iterator = mockk<MongoCursor<String>>()
        every { letterStatDataRepository.iterateLoginsWithUnorderedStats().iterator() } returns iterator

        // When
        val actual = service.getLoginsWithUnorderedStatsSequence()

        // Then
        verify {
            letterStatDataRepository.iterateLoginsWithUnorderedStats()
        }
        confirmVerified(letterStatDataRepository)
        actual.iterator() toBe iterator
    }

    @Test
    fun `should do stats ordering`() {
        // Given
        setUpTransactionWrapperMock(transactionWrapper)
        val sentStat1 = LetterStat(Day(2020, 5, 1), 2)
        val sentStat2 = LetterStat(Day(2020, 5, 5), 4)
        val openStat1 = LetterStat(Day(2021, 5, 1), 1)
        val openStat2 = LetterStat(Day(2020, 6, 5), 2)
        val openStat3 = LetterStat(Day(2020, 5, 9), 3)
        val sentDay1 = Day(2020, 5, 5)
        val sentDay2 = Day(2020, 5, 8)
        val openDay1 = Day(2021, 5, 1)
        val openDay2 = Day(2020, 8, 7)

        val statData = mockk<LetterStatData> {
            every { sentStat } returns listOf(sentStat1, sentStat2)
            every { openStat } returns listOf(openStat1, openStat2, openStat3)
            every { unorderedSent } returns listOf(sentDay1, sentDay2)
            every { unorderedOpen } returns listOf(openDay1, openDay2)
        }
        every { letterStatDataRepository.getStatData(any()) } returns statData

        // When
        service.orderStats(LOGIN)

        // Then
        val slotSent = slot<List<LetterStat>>()
        val slotOpen = slot<List<LetterStat>>()
        verify {
            transactionWrapper.executeInTransaction(any())
            letterStatDataRepository.getStatData(LOGIN)
            letterStatDataRepository.setStatData(LOGIN, capture(slotSent), capture(slotOpen))
        }
        slotSent.captured toContainExactly listOf( // should be used toContainUnorderedExactly~
                LetterStat(Day(2020, 5, 5), 5),
                LetterStat(Day(2020, 5, 8), 1),
                LetterStat(Day(2020, 5, 1), 2)
        )
        slotOpen.captured toContainExactly listOf( // should be used toContainUnorderedExactly~
                LetterStat(Day(2020, 5, 9), 3),
                LetterStat(Day(2021, 5, 1), 2),
                LetterStat(Day(2020, 8, 7), 1),
                LetterStat(Day(2020, 6, 5), 2)
        )
        confirmVerified(transactionWrapper, letterStatDataRepository)
    }
}
package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.model.Email
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.job.DailyMailingJobPayload
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.MailSenderService
import com.kilchichakov.fiveletters.service.UserService
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class DailyMailingJobProcessorTest {

    @RelaxedMockK
    lateinit var mailSenderService: MailSenderService

    @RelaxedMockK
    lateinit var letterService: LetterService

    @RelaxedMockK
    lateinit var userService: UserService

    @InjectMockKs
    lateinit var processor: DailyMailingJobProcessor

    val URL = "someUrl"

    // Sat Sep 14 2019 10:51:49 UTC
    private val instant = Instant.ofEpochMilli(1568458309619)

    @BeforeEach
    fun setUp() {
        processor = DailyMailingJobProcessor(URL, mailSenderService, letterService, userService)
    }

    @Test
    fun `should process payload`() {
        // Given
        val payload = DailyMailingJobPayload("any")
        val letterId1 = ObjectId()
        val letterId2 = ObjectId()
        val letterId3 = ObjectId()
        val letterId4 = ObjectId()
        val user1 = "loupa"
        val user2 = "poupa"
        val user3 = "john"
        val message1 = "dsfdsfl"
        val message2 = "fodo222"
        val message3 = "kdkdkdk"
        val message4 = "0101010"
        val letter1 = Letter(letterId1, user1, message1, false, Date.from(instant), Date.from(instant))
        val letter2 = Letter(letterId2, user2, message2, false, Date.from(instant), Date.from(instant))
        val letter3 = Letter(letterId3, user1, message3, false, Date.from(instant.plusMillis(100)), Date.from(instant))
        val letter4 = Letter(letterId4, user3, message4, false, Date.from(instant), Date.from(instant))
        val nick1 = "Loupeaux"
        val nick2 = "Poupon"
        val nick3 = "Richard"
        val email1 = "lou@pa"
        val email2 = "pou@pa"
        val email3 = "jo@di"
        val userData1 = UserData(null, user1, nick1, email1, true)
        val userData2 = UserData(null, user2, nick2, email2, false)
        val userData3 = UserData(null, user3, nick3, email3, true)

        every { letterService.getLettersForMailing() } returns listOf(letter1, letter2, letter3, letter4)
        every { userService.loadUserData(user1) } returns userData1
        every { userService.loadUserData(user2) } returns userData2
        every { userService.loadUserData(user3) } returns userData3
        val mailed = mutableListOf<Email>()
        var emailCalls = 0
        every { mailSenderService.sendEmail(capture(mailed)) } answers {
            emailCalls += 1
            if (emailCalls == 2) throw RuntimeException()
        }

        // When
        processor.process(payload)

        // Then
        assertThat(mailed).hasSize(2)
        assertThat(mailed.first().to).isEqualTo(email1)
        assertThat(mailed.last().to).isEqualTo(email3)
        assertThat(mailed.first().html).containsSubsequence(nick1, message3, message1)
        assertThat(mailed.last().html).contains(nick3, message4)
        verify(exactly = 1) {
            letterService.getLettersForMailing()
            userService.loadUserData(user1)
            userService.loadUserData(user2)
            userService.loadUserData(user3)
            letterService.markLettersAsMailed(listOf(letterId1.toString(), letterId3.toString()))

        }
        verify(exactly = 2) { mailSenderService.sendEmail(any()) }
        confirmVerified(letterService, userService, mailSenderService)
    }
}
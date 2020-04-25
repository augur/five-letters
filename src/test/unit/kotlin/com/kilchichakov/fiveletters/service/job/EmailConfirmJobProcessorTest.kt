package com.kilchichakov.fiveletters.service.job

import com.kilchichakov.fiveletters.model.Email
import com.kilchichakov.fiveletters.model.job.EmailConfirmSendingJobPayload
import com.kilchichakov.fiveletters.service.MailSenderService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
internal class EmailConfirmJobProcessorTest {

    @RelaxedMockK
    lateinit var mailSenderService: MailSenderService

    @InjectMockKs
    lateinit var processor: EmailConfirmJobProcessor

    val URL = "someUrl"

    @BeforeEach
    fun setUp() {
        processor = EmailConfirmJobProcessor(URL, mailSenderService)
    }

    @Test
    fun `should process payload`() {
        // Given
        val email = "eml"
        val code = "cde"
        val payload = EmailConfirmSendingJobPayload(email, code)
        val sent = slot<Email>()
        every { mailSenderService.sendEmail(capture(sent)) } just runs

        // When
        processor.process(payload)

        // Then
        assertThat(sent.captured.to).isEqualTo(email)
        assertThat(sent.captured.html).contains(code)
        assertThat(sent.captured.html).contains(URL)
    }

}
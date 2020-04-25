package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.config.MailProperties
import com.kilchichakov.fiveletters.exception.ExternalServiceException
import com.kilchichakov.fiveletters.model.Email
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

@ExtendWith(MockKExtension::class)
internal class MailSenderServiceTest {

    @RelaxedMockK
    lateinit var restTemplate: RestTemplate

    @RelaxedMockK
    lateinit var mailProperties: MailProperties

    @InjectMockKs
    lateinit var service: MailSenderService

    val URI = "SOME.URL"
    val FROM = "loupa"

    @BeforeEach
    fun setUp() {
        every { mailProperties.url } returns URI
        every { mailProperties.fromAddress } returns FROM
    }

    @Test
    fun `should send email`() {
        // Given
        val to = "poupa"
        val subj = "somesubj"
        val text = "sometxt"
        val email = Email(to, subj, text)
        val slot = slot<URI>()
        val response = mockk<ResponseEntity<Any>>()
        every { response.statusCode } returns HttpStatus.OK
        every { restTemplate.postForEntity(capture(slot), null,  Any::class.java) } returns response

        // When
        service.sendEmail(email)

        // Then
        assertThat(slot.captured.toString()).contains("$URI?from=$FROM&to=$to&subject=$subj&text=$text")
    }

    @Test
    fun `should throw in case of failure`() {
        // Given
        val response = mockk<ResponseEntity<Any>>()
        every { response.statusCode } returns HttpStatus.INTERNAL_SERVER_ERROR
        every { restTemplate.postForEntity(any<URI>(), null,  Any::class.java) } returns response

        // When
        assertThrows<ExternalServiceException> { service.sendEmail(mockk(relaxed = true)) }
    }
}
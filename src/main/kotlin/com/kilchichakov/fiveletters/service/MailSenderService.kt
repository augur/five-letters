package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.config.MailProperties
import com.kilchichakov.fiveletters.exception.ExternalServiceException
import com.kilchichakov.fiveletters.model.Email
import java.io.ByteArrayOutputStream
import java.net.URI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder


@Service
class MailSenderService {

    @Qualifier("mailingVendorRestTemplate")
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var mailProperties: MailProperties

    private val javaMailSender = JavaMailSenderImpl()

    fun sendEmail(email: Email) {
        LOG.info { "sending email $email" }

        val fileMap: MultiValueMap<String, String> = LinkedMultiValueMap()
        val contentDisposition = ContentDisposition
                .builder("form-data")
                .name("message")
                .filename("message.mime")
                .build()
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())

        val mimeMessage = buildMimeMessage(email)
        val fileEntity = HttpEntity<ByteArray>(mimeMessage, fileMap)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val parts: MultiValueMap<String, Any> = LinkedMultiValueMap()
        parts.add("file", fileEntity)
        parts.add("to", email.to)
        val requestEntity: HttpEntity<MultiValueMap<String, Any>> = HttpEntity(parts, headers)

        val res = restTemplate.postForEntity(mailProperties.url, requestEntity, String::class.java)

        LOG.info { "got response: $res" }

        if (res.statusCode != HttpStatus.OK) {
            throw ExternalServiceException("MailingVendor responded: $res", null)
        }
    }

    private fun buildMimeMessage(email: Email): ByteArray {
        val msg = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(msg, true, "utf-8")
        helper.setFrom(mailProperties.fromAddress)
        helper.addTo(email.to)
        helper.setSubject(email.subject)
        if (email.html != null) {
            helper.setText(email.html, true)
        } else {
            helper.setText(email.text!!, false)
        }
        val out = ByteArrayOutputStream()
        helper.mimeMessage.writeTo(out)
        return out.toByteArray()
    }
}

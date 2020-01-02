package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.config.MailProperties
import com.kilchichakov.fiveletters.exception.ExternalServiceException
import com.kilchichakov.fiveletters.model.Email
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class MailSenderService {

    @Qualifier("mailingVendorRestTemplate")
    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var mailProperties: MailProperties

    fun sendEmail(email: Email) {
        LOG.info { "sending email $email" }

        val uri = createUri(mailProperties.url, mapOf(
                "from" to mailProperties.fromAddress,
                "to" to email.to,
                "subject" to email.subject,
                "text" to email.text
        ))

        LOG.info { "built uri: $uri" }

        val res = restTemplate.postForEntity(uri, null, Any::class.java)

        LOG.info { "got response: $res" }

        if (res.statusCode != HttpStatus.OK) {
            throw ExternalServiceException("MailingVendor responded: $res", null)
        }
    }

    private fun createUri(url: String, params: Map<String, String>): URI {
        val builder = UriComponentsBuilder.fromUriString(url)
        params.forEach { (name, value) ->
            builder.queryParam(name, value)
        }
        return builder.build().toUri()
    }
}
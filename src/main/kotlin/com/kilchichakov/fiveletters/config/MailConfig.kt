package com.kilchichakov.fiveletters.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.web.client.RestTemplate


@Configuration
class MailConfig {

    private val MAILGUN_LOGIN = "api"

    @Bean("mailingVendorRestTemplate")
    fun restTemplate(@Value("\${MAILGUN_API_KEY}") apiKey: String,
                     builder: RestTemplateBuilder): RestTemplate {
        return builder
                .basicAuthentication(MAILGUN_LOGIN, apiKey)
                .build()
    }

    @Bean
    fun mailProperties(@Value("\${MAILGUN_URL}") url: String,
                       @Value("\${FROM_ADDRESS}") from: String): MailProperties {
        return MailProperties(url, from)
    }
}

data class MailProperties(val url: String,
                          val fromAddress: String)
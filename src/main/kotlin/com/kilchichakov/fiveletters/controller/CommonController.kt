package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.service.EmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("common")
class CommonController(
        @Value("\${service.version}") private val serviceVersion: String,
        private val emailService: EmailService
) {

    @GetMapping("/version")
    @Logged
    fun getVersion(): String {
        LOG.info { "asked version" }
        return serviceVersion
                .logResult()
    }

    @GetMapping("/confirmEmail")
    @Logged
    fun confirmEmail(@RequestParam code: String): String {
        LOG.info { "asked to confirmEmail with code $code" }
        emailService.confirmEmailByCode(code)
        return "Email has been confirmed successfully!"
    }
}
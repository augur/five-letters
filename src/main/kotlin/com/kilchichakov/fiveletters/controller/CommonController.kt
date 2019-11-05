package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("common")
class CommonController(
        @Value("\${service.version}") private val serviceVersion: String
) {

    @GetMapping("/version")
    @Logged
    fun getVersion(): String {
        LOG.info { "asked version" }
        return serviceVersion
                .logResult()
    }
}
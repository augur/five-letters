package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.service.LetterService
import com.kilchichakov.fiveletters.service.SystemService
import com.kilchichakov.fiveletters.service.UserService
import com.mongodb.client.MongoDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("admin")
@Secured("ROLE_ADMIN")
class AdminController {

    @Autowired
    lateinit var db: MongoDatabase

    @Autowired
    lateinit var systemService: SystemService

    @GetMapping("/whoami")
    fun whoAmI(): String {
        LOG.info { "asked whoami" }
        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        return "hello, ${auth.name}"
                .logResult()
    }

    @PostMapping("/registration")
    @Logged
    fun switchRegistration(@RequestParam enabled: Boolean) {
        LOG.info { "asked to switch registration to $enabled" }
        ControllerUtils.getLogin()
        systemService.switchRegistration(enabled)
    }
}
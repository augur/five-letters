package com.kilchichakov.fiveletters.controller

import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("admin")
@Secured("ROLE_ADMIN")
class AdminController {

    @GetMapping("/whoami")
    fun whoAmI(): String {
        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        return "hello, ${auth.name}"
    }
}
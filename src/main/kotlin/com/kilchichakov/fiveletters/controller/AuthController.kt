package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping



@RestController
@RequestMapping("auth")
class AuthController {

    @Autowired
    private lateinit var authService: AuthService

    @PostMapping
    fun authenticate(@RequestBody authRequest: AuthRequest): AuthResponse {
        return authService.authenticate(authRequest.login, authRequest.password)
    }

    @GetMapping("/whoami")
    fun whoAmI(): String {
        val auth = SecurityContextHolder.getContext().authentication as UsernamePasswordAuthenticationToken
        return "hello, ${auth.name}"
    }
}
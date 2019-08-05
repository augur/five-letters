package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.model.dto.AuthToken
import com.kilchichakov.fiveletters.model.dto.Credentials
import com.kilchichakov.fiveletters.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
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

    @PostMapping("/do")
    fun authenticate(@RequestBody credentials: Credentials): AuthToken {
        return authService.authenticate(credentials.login, credentials.password)
    }

    @GetMapping("/hi")
    fun helloWorld(): String {
        return "hello!!"
    }
}
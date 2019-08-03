package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.model.User
import com.kilchichakov.fiveletters.service.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping



@RestController
class AuthController {

    @Autowired
    private lateinit var jwtService: JwtService

    @RequestMapping("/hello")
    fun firstPage(): String {
        val user = User(null, "denis", "qwerty", "augur")

        return "Hello World " + jwtService.generateToken(user)
    }

    @PostMapping("/world")
    fun second(@RequestBody body: String): String {

        return "Hello World " + jwtService.validateToken(body)
    }
}
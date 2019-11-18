package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.service.AuthService
import com.kilchichakov.fiveletters.service.InputValidationService
import com.kilchichakov.fiveletters.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("auth")
class AuthController {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    protected lateinit var inputValidationService: InputValidationService

    @PostMapping
    @Logged
    fun authenticate(@RequestBody authRequest: AuthRequest): AuthResponse {
        inputValidationService.validate(authRequest)
        LOG.info { "authentication request, login: ${authRequest.login}" }
        val result = authService.authenticate(authRequest.login, authRequest.password)
        LOG.info { "success" }
        return result.logResult()
    }

}
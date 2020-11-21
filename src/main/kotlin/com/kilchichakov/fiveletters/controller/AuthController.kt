package com.kilchichakov.fiveletters.controller

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.aspect.Logged
import com.kilchichakov.fiveletters.model.dto.AuthGoogleResponse
import com.kilchichakov.fiveletters.model.dto.AuthRefreshRequest
import com.kilchichakov.fiveletters.model.dto.AuthRequest
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.service.AuthGoogleService
import com.kilchichakov.fiveletters.service.AuthService
import com.kilchichakov.fiveletters.service.InputValidationService
import org.springframework.beans.factory.annotation.Autowired
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
    private lateinit var authGoogleService: AuthGoogleService

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

    @PostMapping("/google")
    @Logged
    fun googleAuthenticate(@RequestBody idTokenString: String): AuthGoogleResponse {
        LOG.info { "request to authorize by Google id_token" }
        return authGoogleService.authenticate(idTokenString).logResult()
    }

    @PostMapping("/refresh")
    @Logged
    fun refreshAuth(@RequestBody request: AuthRefreshRequest): AuthResponse {
        inputValidationService.validate(request)
        LOG.info { "request to refresh auth, login=${request.login}" }
        return authService.refreshAuth(request.login, request.refreshToken).logResult()
    }
}
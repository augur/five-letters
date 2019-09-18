package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service


@Service
class AuthService {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var jwtService: JwtService

    fun authenticate(username: String, password: String): AuthResponse {
        try {
            LOG.info { "authenticating" }
            val authed = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
            LOG.info { "authed is $authed" }
            val userDetails = authed.principal as UserDetails
            LOG.info { "userDetails are $userDetails" }
            return AuthResponse(jwtService.generateToken(userDetails))
        } catch (e: DisabledException) {
            throw Exception("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw Exception("INVALID_CREDENTIALS", e)
        }
    }
}
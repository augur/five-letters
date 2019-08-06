package com.kilchichakov.fiveletters.service

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
            val authed = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
            val userDetails = authed.principal as UserDetails
            return AuthResponse(jwtService.generateToken(userDetails))
        } catch (e: DisabledException) {
            throw Exception("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw Exception("INVALID_CREDENTIALS", e)
        }
    }
}
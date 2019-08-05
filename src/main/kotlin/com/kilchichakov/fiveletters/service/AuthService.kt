package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.dto.AuthToken
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails


@Service
class AuthService {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var jwtService: JwtService

    fun authenticate(username: String, password: String): AuthToken {
        try {
            val authed = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
            val userDetails = authed.principal as UserDetails
            return AuthToken(jwtService.generateToken(userDetails))
        } catch (e: DisabledException) {
            throw Exception("USER_DISABLED", e)
        } catch (e: BadCredentialsException) {
            throw Exception("INVALID_CREDENTIALS", e)
        }
    }
}
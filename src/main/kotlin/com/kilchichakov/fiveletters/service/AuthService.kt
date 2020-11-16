package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import java.time.Clock
import java.util.Date
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service


@Service
class AuthService {

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var refreshTokenService: RefreshTokenService

    fun authenticate(username: String, password: String): AuthResponse {
        LOG.info { "authenticating" }
        val authed = authenticationManager.authenticate(UsernamePasswordAuthenticationToken(username, password))
        LOG.info { "authed is $authed" }
        val userDetails = authed.principal as UserDetails
        LOG.info { "userDetails are $userDetails" }
        val encodedJwt = jwtService.generateToken(userDetails)
        val refreshToken = refreshTokenService.generateRefreshToken(username)
        return AuthResponse(
                login = username,
                jwt = encodedJwt.code,
                jwtDueDate = encodedJwt.dueDate,
                refreshToken = refreshToken.code,
                refreshTokenDueDate = refreshToken.dueDate
        )
    }
}
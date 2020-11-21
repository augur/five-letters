package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import java.time.Clock
import java.util.Date
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
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

    @Autowired
    private lateinit var authDataRepository: AuthDataRepository

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

    fun refreshAuth(login: String, refreshTokenCode: String): AuthResponse {
        LOG.info { "refreshing auth for $login" }
        val authData = authDataRepository.loadUserData(login) ?: throw DataException("AuthData for login=$login not found")

        if (refreshTokenService.validateRefreshToken(tokenCode = refreshTokenCode, authData)) {
            LOG.info { "validation passed" }
            val encodedJwt = jwtService.generateToken(authData)
            val refreshToken = refreshTokenService.generateRefreshToken(login)
            return AuthResponse(
                    login = login,
                    jwt = encodedJwt.code,
                    jwtDueDate = encodedJwt.dueDate,
                    refreshToken = refreshToken.code,
                    refreshTokenDueDate = refreshToken.dueDate
            )
        } else {
            throw BadCredentialsException("refreshAuth failed due to refreshToken didn't pass validation")
        }
    }
}
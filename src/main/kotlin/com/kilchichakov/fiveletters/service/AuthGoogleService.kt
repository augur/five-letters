package com.kilchichakov.fiveletters.service

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.FoundEmailUnconfirmed
import com.kilchichakov.fiveletters.model.FoundOk
import com.kilchichakov.fiveletters.model.NotFound
import com.kilchichakov.fiveletters.model.authorities
import com.kilchichakov.fiveletters.model.dto.AuthEmailNotFound
import com.kilchichakov.fiveletters.model.dto.AuthEmailUnconfirmed
import com.kilchichakov.fiveletters.model.dto.AuthGoogleResponse
import com.kilchichakov.fiveletters.model.dto.AuthResponse
import com.kilchichakov.fiveletters.model.dto.AuthSuccess
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthGoogleService {

    @Autowired
    private lateinit var authDataRepository: AuthDataRepository

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var verifier: GoogleIdTokenVerifier

    @Autowired
    private lateinit var jobService: JobService

    @Autowired
    private lateinit var refreshTokenService: RefreshTokenService

    fun authenticate(idTokenString: String): AuthGoogleResponse {
        val tokenPart = idTokenString.substring(0, 20) + "..."
        LOG.info { "authenticating with idToken $tokenPart" }
        val idToken = verifier.verify(idTokenString)
                ?: throw BadCredentialsException("Token $tokenPart not passed")
        val email = idToken.payload.email
        LOG.info { "acquired email=$email" }

        when (val searchResult = authDataRepository.findAuthDataByEmail(email)) {
            is FoundOk -> {
                LOG.info { "email=$email found OK" }
                val jwt = jwtService.generateToken(searchResult.authData)
                val refreshToken = refreshTokenService.generateRefreshToken(searchResult.authData.login)
                return AuthSuccess(auth = AuthResponse(
                        login = searchResult.authData.login,
                        jwt = jwt.code,
                        jwtDueDate = jwt.dueDate,
                        refreshToken = refreshToken.code,
                        refreshTokenDueDate = refreshToken.dueDate,
                ))
            }
            is FoundEmailUnconfirmed -> {
                LOG.info { "$email is unconfirmed, scheduling confirmation" }
                jobService.scheduleEmailConfirmation(searchResult.authData.login)
                return AuthEmailUnconfirmed(email = email)
            }
            NotFound -> {
                LOG.info { "$email not found, should be registered first" }
                return AuthEmailNotFound(email = email)
            }
        }
    }
}
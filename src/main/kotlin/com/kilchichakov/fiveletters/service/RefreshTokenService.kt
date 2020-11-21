package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import com.kilchichakov.fiveletters.util.now
import java.time.Clock
import java.util.Date
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service

@Service
class RefreshTokenService(
        @Value("\${REFRESH_TTL}") private val refreshTokenTtlSeconds: Long,
        private val authDataRepository: AuthDataRepository,
        private val clock: Clock,
) {

    fun generateRefreshToken(login: String): RefreshToken {
        LOG.info { "generating refresh token for $login" }
        val dueDate = Date.from(clock.instant().plusSeconds(refreshTokenTtlSeconds))
        LOG.info { "token will expire on $dueDate" }
        val tokenCode = UUID.randomUUID().toString()
        if (!authDataRepository.setRefreshToken(login, tokenCode, dueDate))
            throw DatabaseException("New refreshToken wasn't saved for $login")
        return RefreshToken(tokenCode, dueDate)
    }

    fun validateRefreshToken(tokenCode: String, authData: AuthData): Boolean {
        if (clock.now() > authData.refreshTokenDueDate) return false
                .also { LOG.warn { "refreshToken expired for login=${authData.login}" } }
        if (authData.refreshToken != tokenCode) return false
                .also { LOG.warn { "refreshToken doesn't match" } }
        return true
    }

    data class RefreshToken(
            val code: String,
            val dueDate: Date,
    )
}
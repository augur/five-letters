package com.kilchichakov.fiveletters.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.authorities
import com.kilchichakov.fiveletters.util.now
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import org.springframework.security.core.userdetails.User


@Service
class JwtService(
        @Value("\${JWT_SECRET}") secret: String,
        @Value("\${JWT_ISSUER}") private val issuer: String,
        @Value("\${JWT_TTL}") private val ttlSeconds: Long,
        private val clock: Clock
) {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .acceptLeeway(1)
            .build()

    fun generateToken(userDetails: UserDetails): EncodedJwt {
        val expires = Date.from(clock.instant().plus(ttlSeconds, ChronoUnit.SECONDS))
        val roles = (userDetails.authorities.map { a -> a.authority }).toTypedArray()
        LOG.info { "generating token: ${userDetails.username}, $issuer, ${expires}, $algorithm, ${roles.toList()}" }
        val code = JWT.create()
                .withIssuer(issuer)
                .withSubject(userDetails.username)
                .withExpiresAt(expires)
                .withArrayClaim("roles", roles)
                .sign(algorithm)
        return EncodedJwt(code, expires)
    }

    fun generateToken(authData: AuthData): EncodedJwt {
        val user: UserDetails = User(authData.login, "", authData.authorities)
        LOG.info { "built user=$user for JWT token" }
        return generateToken(user)
    }

    /**
     * @return login of user, or null if not authorized
     */
    fun validateToken(token: String): DecodedJwt {
        return try {
            val jwt = verifier.verify(token)!!
            val roles = jwt.getClaim("roles").asList(String::class.java).orEmpty()
            DecodedJwt(jwt.subject, roles, jwt.expiresAt)
        } catch (e : Exception) {
            LOG.error(e) { "caught during token $token validation" }
            DecodedJwt(null, emptyList(), Date.from(Instant.EPOCH))
        }
    }

    data class DecodedJwt(
            val username: String?,
            val roles: List<String>,
            val expiresAt: Date
    )

    data class EncodedJwt(
            val code: String,
            val dueDate: Date,
    )
}
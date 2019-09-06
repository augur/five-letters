package com.kilchichakov.fiveletters.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kilchichakov.fiveletters.model.UserData
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.Calendar


@Service
class JwtService(
        @Value("\${JWT_SECRET}") secret: String,
        @Value("\${JWT_ISSUER}") private val issuer: String,
        @Value("\${JWT_TTL}") private val ttlSeconds: Int
) {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .acceptLeeway(1)
            .build()


    fun generateToken(userDetails: UserDetails): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, ttlSeconds)
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userDetails.username)
                .withExpiresAt(calendar.time)
                .withArrayClaim("roles", (userDetails.authorities.map { a -> a.authority }).toTypedArray())
                .sign(algorithm)
    }

    /**
     * @return login of user, or null if not authorized
     */
    fun validateToken(token: String): DecodedJwt {
        return try {
            val jwt = verifier.verify(token)!!
            val roles = jwt.getClaim("roles").asList(String::class.java).orEmpty()
            DecodedJwt(jwt.subject, roles)
        } catch (e : Exception) {
            DecodedJwt(null, emptyList())
        }
    }

    data class DecodedJwt(
            val username: String?,
            val roles: List<String>
    )
}
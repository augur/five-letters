package com.kilchichakov.fiveletters.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kilchichakov.fiveletters.model.UserData
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service


@Service
class JwtService(
        @Value("\${jwt.secret}") secret: String,
        @Value("\${jwt.issuer}") private val issuer: String
) {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()


    fun generateToken(userDetails: UserDetails): String {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(userDetails.username)
                .sign(algorithm)
    }

    /**
     * @return login of user, or null if not authorized
     */
    fun validateToken(token: String): String? {
        return try {
            val jwt = verifier.verify(token)!!
            jwt.subject
        } catch (e : Exception) {
            null
        }
    }

}
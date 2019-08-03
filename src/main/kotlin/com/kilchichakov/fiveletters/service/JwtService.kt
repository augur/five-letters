package com.kilchichakov.fiveletters.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kilchichakov.fiveletters.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.auth0.jwt.interfaces.DecodedJWT



@Service
class JwtService(
        @Value("\${jwt.secret}") secret: String,
        @Value("\${jwt.issuer}") private val issuer: String
) {

    private val algorithm = Algorithm.HMAC256(secret)
    private val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()


    fun generateToken(user: User): String {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.login)
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
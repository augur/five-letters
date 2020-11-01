package com.kilchichakov.fiveletters.model.dto

sealed class AuthGoogleResponse
class AuthSuccess(val jwt: String): AuthGoogleResponse() {
    val type = "ok"
    override fun toString(): String {
        return "AuthSuccess(type='$type', jwt(hash)='${jwt.hashCode()}')"
    }
}
class AuthEmailUnconfirmed(val email: String): AuthGoogleResponse() {
    val type = "unconfirmed"
}
class AuthEmailNotFound(val email: String): AuthGoogleResponse() {
    val type: String = "notfound"
}
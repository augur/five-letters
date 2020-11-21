package com.kilchichakov.fiveletters.model.dto

sealed class AuthGoogleResponse
class AuthSuccess(val auth: AuthResponse): AuthGoogleResponse() {
    val type = "ok"
}
class AuthEmailUnconfirmed(val email: String): AuthGoogleResponse() {
    val type = "unconfirmed"
}
class AuthEmailNotFound(val email: String): AuthGoogleResponse() {
    val type: String = "notfound"
}
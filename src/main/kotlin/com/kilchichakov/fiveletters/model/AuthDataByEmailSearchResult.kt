package com.kilchichakov.fiveletters.model

sealed class AuthDataByEmailSearchResult
class FoundOk(val authData: AuthData) : AuthDataByEmailSearchResult()
class FoundEmailUnconfirmed(val authData: AuthData) : AuthDataByEmailSearchResult()
object NotFound : AuthDataByEmailSearchResult()

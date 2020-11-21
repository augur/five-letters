package com.kilchichakov.fiveletters.model

import java.util.Date
import org.bson.types.ObjectId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class AuthData(
        val _id: ObjectId?,
        val login: String,
        val password: String,
        val admin: Boolean = false,
        var refreshToken: String? = null,
        var refreshTokenDueDate: Date? = null,
) {
    override fun toString(): String {
        return "UserData(_id=$_id, login='$login', password=********, admin=$admin, refreshToken(hash)=${refreshToken.hashCode()}" +
                ", refreshTokenDueDate=$refreshTokenDueDate)"
    }
}

val AuthData.authorities: List<GrantedAuthority>
        get() = if (admin) listOf(SimpleGrantedAuthority("ROLE_ADMIN")) else emptyList()
package com.kilchichakov.fiveletters.filter

import com.kilchichakov.fiveletters.service.JwtService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.tomcat.jni.User.username
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Service
import java.lang.Exception


@Component
class JwtRequestFilter : OncePerRequestFilter() {

    companion object {
        private const val HEADER_NAME = "Authorization"
        private const val TOKEN_PREFIX = "Bearer "
        private const val PASSWORD_PLACEHOLDER = "placeholder"
    }

    @Autowired
    private lateinit var jwtService: JwtService

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val requestTokenHeader = request.getHeader(HEADER_NAME).orEmpty()

        if (requestTokenHeader.startsWith(TOKEN_PREFIX)) {

            val jwtToken = requestTokenHeader.substring(TOKEN_PREFIX.length)
            val (userName, roles) = jwtService.validateToken(jwtToken)

            if (userName != null && SecurityContextHolder.getContext().authentication == null) {
                val authorities = roles.map { SimpleGrantedAuthority(it) }
                val user = User(userName, PASSWORD_PLACEHOLDER, authorities)
                val authToken = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }

}
package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.repository.UserDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService : UserDetailsService {

    @Autowired
    private lateinit var userDataDataRepository: UserDataRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun registerNewUser(login: String, password: String) {
        val userData = UserData(null, login, passwordEncoder.encode(password), "")
        userDataDataRepository.insertNewUser(userData)
    }

    override fun loadUserByUsername(login: String): UserDetails {
        val data = userDataDataRepository.loadUserData(login)!!
        val authorities = if (data.admin) listOf(SimpleGrantedAuthority("ROLE_ADMIN")) else emptyList()
        return User(data.login, data.password, authorities)
    }
}

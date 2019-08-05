package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.repository.UserDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class UserService : UserDetailsService {

    @Autowired
    private lateinit var userDataDataRepository: UserDataRepository

    override fun loadUserByUsername(login: String): UserDetails {
        val data = userDataDataRepository.loadUserData(login)!!
        return User(data.login, data.password, emptyList())
    }
}

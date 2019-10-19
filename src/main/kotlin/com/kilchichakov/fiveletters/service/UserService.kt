package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.exception.TermsOfUseException
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService : UserDetailsService {

    @Autowired
    private lateinit var transactionWrapper: TransactionWrapper

    @Autowired
    private lateinit var passCodeService: PassCodeService

    @Autowired
    private lateinit var userDataDataRepository: UserDataRepository

    @Autowired
    private lateinit var systemStateRepository: SystemStateRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun registerNewUser(login: String, password: String, licenceAccepted: Boolean, code: String?) {
        LOG.info { "registering new user $login" }
        if (!licenceAccepted) throw TermsOfUseException("Licence was not accepted")
        if (!systemStateRepository.read().registrationEnabled) throw SystemStateException("Registration is disabled")

        val userData = UserData(null, login, passwordEncoder.encode(password), "")
        transactionWrapper.executeInTransaction {
            val passCode = passCodeService.getPassCode(code!!) //TODO omit passcode based on system state
            passCodeService.usePassCode(passCode, login, it)
            userDataDataRepository.insertNewUser(userData, it)
        }

    }

    override fun loadUserByUsername(login: String): UserDetails {
        LOG.info { "loading by login $login" }
        val data = userDataDataRepository.loadUserData(login)!!
        val authorities = if (data.admin) listOf(SimpleGrantedAuthority("ROLE_ADMIN")) else emptyList()
        return User(data.login, data.password, authorities)
    }
}

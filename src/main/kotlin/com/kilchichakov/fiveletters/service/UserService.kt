package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.exception.TermsOfUseException
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import com.kilchichakov.fiveletters.repository.AuthDataRepository
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
    private lateinit var authDataRepository: AuthDataRepository

    @Autowired
    private lateinit var systemStateRepository: SystemStateRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun registerNewUser(login: String, password: String, licenceAccepted: Boolean, code: String?) {
        LOG.info { "registering new user $login" }
        if (!licenceAccepted) throw TermsOfUseException("Licence was not accepted")
        if (!systemStateRepository.read().registrationEnabled) throw SystemStateException("Registration is disabled")

        val userData = AuthData(null, login, passwordEncoder.encode(password))
        transactionWrapper.executeInTransaction {
            LOG.info { "resolving passcode $code" }
            val passCode = passCodeService.getPassCode(code!!) //TODO omit passcode based on system state
            LOG.info { "found $passCode, consuming" }
            passCodeService.usePassCode(passCode, login, it)
            LOG.info { "consumed" }
            authDataRepository.insertNewUser(userData, it)
        }

    }

    override fun loadUserByUsername(login: String): UserDetails {
        LOG.info { "loading by login $login" }
        val data = authDataRepository.loadUserData(login)!!
        val authorities = if (data.admin) listOf(SimpleGrantedAuthority("ROLE_ADMIN")) else emptyList()
        return User(data.login, data.password, authorities)
    }

    fun changeUserPassword(login: String, rawPassword: String) {
        LOG.info { "changing password of $login" }
        val encoded = passwordEncoder.encode(rawPassword)
        if (!authDataRepository.changePassword(login, encoded)) throw DatabaseException("Unexpected update result during changing user $login password")
    }
}

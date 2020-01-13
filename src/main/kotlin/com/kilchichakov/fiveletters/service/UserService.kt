package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.exception.SystemStateException
import com.kilchichakov.fiveletters.exception.TermsOfUseException
import com.kilchichakov.fiveletters.model.AuthData
import com.kilchichakov.fiveletters.model.UserData
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import com.kilchichakov.fiveletters.repository.AuthDataRepository
import com.kilchichakov.fiveletters.repository.UserDataRepository
import com.mongodb.client.ClientSession
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

    @Autowired
    private lateinit var userDataRepository: UserDataRepository

    @Autowired
    private lateinit var jobService: JobService

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
        LOG.info { "loading UserDetails by login $login" }
        val data = authDataRepository.loadUserData(login)!!
        val authorities = if (data.admin) listOf(SimpleGrantedAuthority("ROLE_ADMIN")) else emptyList()
        return User(data.login, data.password, authorities)
    }

    fun changeUserPassword(login: String, rawPassword: String) {
        LOG.info { "changing password of $login" }
        val encoded = passwordEncoder.encode(rawPassword)
        if (!authDataRepository.changePassword(login, encoded)) throw DatabaseException("Unexpected update result during changing user $login password")
    }

    fun loadUserData(login: String): UserData {
        LOG.info { "loading UserData by login $login" }
        return userDataRepository.loadUserData(login) ?: throw DataException("not found UserData of $login")
    }

    fun updateUserData(login: String, email: String, nickname: String) {
        LOG.info { "updating userData of $login - $email, $nickname" }
        val updateResult = userDataRepository.updateUserData(login, email, nickname)
        if (!updateResult.success) throw DatabaseException("Unexpected update result during changing user $login userData")
        if (updateResult.emailChanged) jobService.scheduleEmailConfirmation(login)
    }

    fun setConfirmationCode(login: String, code: String, clientSession: ClientSession) {
        LOG.info { "setting confirmation code of $login to $code" }
        if (!userDataRepository.setEmailConfirmationCode(login, code, clientSession))
            throw DatabaseException("Unexpected update result during setting user $login confirmation code")
    }
}

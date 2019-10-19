package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.OneTimePassCode
import com.kilchichakov.fiveletters.model.PassCode
import com.kilchichakov.fiveletters.repository.PassCodeRepository
import com.mongodb.client.ClientSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.Date
import java.util.UUID

@Service
class PassCodeService(
        private val clock: Clock,
        private val passCodeRepository: PassCodeRepository
) {

    fun getPassCode(code: String): PassCode {
        return passCodeRepository.findPassCode(code) ?: throw DataException("No passCode associated with $code")
    }

    fun generateOneTimePassCode(secondsValid: Long): OneTimePassCode {
        val validUntil = Date.from(clock.instant().plusMillis(secondsValid * 1000))
        val passCode = OneTimePassCode(generateUUID(), validUntil)
        LOG.info { "new one-time passcode: $passCode" }
        passCodeRepository.insertPassCode(passCode)
        LOG.info { "generated" }
        return passCode
    }

    fun usePassCode(passCode: PassCode, login: String, clientSession: ClientSession) {
        when(passCode) {
            is OneTimePassCode -> passCodeRepository.consumeOneTimePassCode(passCode._id,login, clientSession)
        }
    }

    private fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

}
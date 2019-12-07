package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.repository.UserDataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    private lateinit var userDataRepository: UserDataRepository

    fun confirmEmailByCode(code: String) {
        LOG.info { "confirming email by code $code" }
        if (userDataRepository.setEmailConfirmed(code)) {
            LOG.info { "confirmed" }
        } else {
            throw DatabaseException("Unexpected update result while confirming email")
        }
    }
}
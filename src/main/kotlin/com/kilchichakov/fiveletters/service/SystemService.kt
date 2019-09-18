package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.repository.SystemStateRepository
import org.springframework.stereotype.Service

@Service
class SystemService(
        private val systemStateRepository: SystemStateRepository
) {

    fun switchRegistration(enable: Boolean) {
        LOG.info { "switching registration to $enable" }
        systemStateRepository.switchRegistration(enable)
        LOG.info { "switched" }
    }
}
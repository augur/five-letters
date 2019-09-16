package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.repository.SystemStateRepository
import org.springframework.stereotype.Service

@Service
class SystemService(
        private val systemStateRepository: SystemStateRepository
) {

    fun switchRegistration(enable: Boolean) {
        systemStateRepository.switchRegistration(enable)
    }
}
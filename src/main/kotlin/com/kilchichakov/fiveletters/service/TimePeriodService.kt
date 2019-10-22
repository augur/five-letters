package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.TimePeriod
import com.kilchichakov.fiveletters.repository.TimePeriodRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TimePeriodService {

    @Autowired
    private lateinit var timePeriodRepository: TimePeriodRepository

    fun getTimePeriod(name: String): TimePeriod {
        return timePeriodRepository.getTimePeriod(name)
    }

    fun listAllTimePeriods(): List<String> {
        return timePeriodRepository.listTimePeriods()
    }
}
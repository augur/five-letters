package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterPeriodType
import com.kilchichakov.fiveletters.repository.LetterRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Calendar
import java.util.Date

@Service
class LetterService {

    @Autowired
    lateinit var letterRepository: LetterRepository

    fun sendLetter(login: String, message: String, period: LetterPeriodType) {
        val letter = Letter(null, login, message, false, calcOpenDate(period))
        letterRepository.saveNewLetter(letter)
    }


    private fun calcOpenDate(periodType: LetterPeriodType): Date {
        val calendar = Calendar.getInstance()
        //calendar.time = SimpleDateFormat("YYYY-MM-dd").parse("2018-12-31")
        when(periodType) {
            LetterPeriodType.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            LetterPeriodType.MONTH -> calendar.add(Calendar.MONTH, 1)
            LetterPeriodType.THREE_MONTHS -> calendar.add(Calendar.MONTH, 3)
            LetterPeriodType.YEAR -> calendar.add(Calendar.YEAR, 1)
            LetterPeriodType.THREE_YEARS -> calendar.add(Calendar.YEAR, 3)
        }
        return calendar.time
    }
}
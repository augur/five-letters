package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterPeriodType
import com.kilchichakov.fiveletters.repository.LetterRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Service
class LetterService {

    @Autowired
    lateinit var letterRepository: LetterRepository

    fun sendLetter(login: String, message: String, period: LetterPeriodType, timezoneOffset: Int) {
        val letter = Letter(null, login, message, false, Calendar.getInstance().time, calcOpenDate(period, timezoneOffset))
        letterRepository.saveNewLetter(letter)
    }

    fun getNewLetters(login: String): List<Letter> {
        return letterRepository.getNewLetters(login)
    }

    private fun calcOpenDate(periodType: LetterPeriodType, timezoneOffset: Int): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        //calendar.time = SimpleDateFormat("YYYY-MM-dd").parse("2018-12-31")
        when(periodType) {
            LetterPeriodType.WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            LetterPeriodType.MONTH -> calendar.add(Calendar.MONTH, 1)
            LetterPeriodType.THREE_MONTHS -> calendar.add(Calendar.MONTH, 3)
            LetterPeriodType.YEAR -> calendar.add(Calendar.YEAR, 1)
            LetterPeriodType.THREE_YEARS -> calendar.add(Calendar.YEAR, 3)
        }
        // Setting to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        //Applying offset
        calendar.add(Calendar.MINUTE, timezoneOffset)
        return calendar.time
    }
}
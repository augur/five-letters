package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.TimePeriod
import com.kilchichakov.fiveletters.model.dto.PageRequest
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

    @Autowired
    lateinit var timePeriodService: TimePeriodService

    fun sendLetter(login: String, message: String, periodName: String, timezoneOffset: Int) {
        val period = timePeriodService.getTimePeriod(periodName)
        val letter = Letter(null, login, message, false, Calendar.getInstance().time, calcOpenDate(period, timezoneOffset))
        LOG.info { "sending letter $letter" }
        letterRepository.saveNewLetter(letter)
        LOG.info { "sent" }
    }

    fun getNewLetters(login: String): List<Letter> {
        LOG.info { "getting new letters for user $login" }
        return letterRepository.getNewLetters(login)
    }

    fun getInboxPage(login: String, pageRequest: PageRequest): Page<Letter> {
        LOG.info { "request inbox page with $pageRequest for user $login" }
        return letterRepository.inbox(
                login = login,
                skip = pageRequest.pageSize * (pageRequest.pageNumber - 1),
                limit = pageRequest.pageSize,
                includeRead = pageRequest.includeRead,
                includeMailed = pageRequest.includeMailed,
                includeArchived = pageRequest.includeArchived
        )
    }

    fun getLettersForMailing(): List<Letter> {
        LOG.info { "getting new letters for mail sending" }
        return letterRepository.getLettersForMailing()
    }

    fun getFutureLetters(login: String): List<SealedLetterEnvelop> {
        LOG.info { "getting future letters for user $login, limit 5000" }
        return letterRepository.getFutureLetters(login, 5000)
    }

    fun markLetterAsRead(login: String, letterId: String) {
        LOG.info { "marking letter $letterId of user $login as read" }
        if (!letterRepository.markLetterAsRead(login, letterId)) {
            throw DatabaseException("Unexpected update result during markLetterAsRead()")
        }
    }

    fun markLettersAsMailed(letterIds: List<String>) {
        LOG.info { "marking letters with ids $letterIds as mail sent" }
        if (!letterRepository.markLettersAsMailed(letterIds)) {
            throw DatabaseException("Unexpected update result during markLettersAsMailed()")
        }
    }

    internal fun calcOpenDate(period: TimePeriod, timezoneOffset: Int): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        //calendar.time = SimpleDateFormat("YYYY-MM-dd").parse("2018-12-31")
        with(period) {
            calendar.add(Calendar.DAY_OF_YEAR, days)
            calendar.add(Calendar.WEEK_OF_YEAR, weeks)
            calendar.add(Calendar.MONTH, months)
            calendar.add(Calendar.YEAR, years)
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
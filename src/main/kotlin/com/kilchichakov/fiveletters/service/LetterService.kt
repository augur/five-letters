package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.Page
import com.kilchichakov.fiveletters.model.SealedLetterEnvelop
import com.kilchichakov.fiveletters.model.TimePeriod
import com.kilchichakov.fiveletters.model.dto.PageRequest
import com.kilchichakov.fiveletters.repository.LetterRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@Service
class LetterService {

    @Autowired
    lateinit var letterRepository: LetterRepository

    @Autowired
    lateinit var timePeriodService: TimePeriodService

    @Autowired
    lateinit var letterStatDataService: LetterStatDataService

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var transactionWrapper: TransactionWrapper

    fun sendLetter(login: String, message: String, periodName: String) {
        val timezone = userService.loadUserData(login).timeZone
        val period = timePeriodService.getTimePeriod(periodName)
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
        val letter = Letter(null, login, message, false, Calendar.getInstance().time,
                calcOpenDate(period, calendar))
        doSend(letter, timezone)
    }

    fun sendLetter(login: String, message: String, openDate: Day) {
        val timezone = userService.loadUserData(login).timeZone
        val letter = Letter(null, login, message, false, Calendar.getInstance().time, openDate.toDate(timezone))
        doSend(letter, timezone)
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
                includeArchived = pageRequest.includeArchived,
                sortBy = pageRequest.sortBy
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

    fun getLettersDatesSequence(login: String): Sequence<SealedLetterEnvelop> {
        LOG.info { "getting sequence of all letters for user=$login" }
        return letterRepository.iterateLettersDates(login).iterator().asSequence()
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

    internal fun calcOpenDate(period: TimePeriod, calendar: Calendar): Date {
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
        return calendar.time
    }

    private fun doSend(letter: Letter, timezone: String) {
        LOG.info { "sending letter $letter" }
        transactionWrapper.executeInTransaction {
            letterRepository.saveNewLetter(letter)
            letterStatDataService.addLetterStats(letter, timezone)
        }
        LOG.info { "sent" }
    }

    private fun Day.toDate(timezone: String): Date {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone))
        calendar.set(Calendar.YEAR, year.toInt())
        calendar.set(Calendar.MONTH, month.toInt() - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day.toInt())
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
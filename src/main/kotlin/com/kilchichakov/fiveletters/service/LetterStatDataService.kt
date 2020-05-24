package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DatabaseException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.Letter
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.model.LetterStatData
import com.kilchichakov.fiveletters.model.dto.GetLetterStatResponse
import com.kilchichakov.fiveletters.repository.LetterStatDataRepository
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.Date


@Service
class LetterStatDataService(
        private val repository: LetterStatDataRepository,
        private val transactionWrapper: TransactionWrapper,
        private val letterService: LetterService,
        private val userService: UserService
) {

    fun getLetterStats(login: String): GetLetterStatResponse {
        LOG.info { "preparing letter stat data for user=$login" }
        val statData = loadStatData(login)
        return GetLetterStatResponse(
                orderStat(statData.sentStat, statData.unorderedSent),
                orderStat(statData.openStat, statData.unorderedOpen)
        )
    }

    fun recalculateStatData(login: String) {
        LOG.info { "recalculate letter stat data for user=$login" }
        val timezone = userService.loadUserData(login).timeZone
        val letters = letterService.getLettersDatesSequence(login)
        val sentMap = HashMap<Day, Int>()
        val openMap = HashMap<Day, Int>()
        letters.forEach {
            val sentDay = it.sendDate.toDay(timezone)
            val openDay = it.openDate.toDay(timezone)
            sentMap.merge(sentDay, 1, Int::plus)
            openMap.merge(openDay, 1, Int::plus)
        }
        val sentStats = sentMap.map { LetterStat(it.key, it.value) }
        val openStats = openMap.map { LetterStat(it.key, it.value) }

        repository.setStatData(login, sentStats, openStats)
        LOG.info { "recalculation done"}
    }

    fun addLetterStats(letter: Letter, timezone: String) {
        LOG.info { "adding stats of letter=$letter" }
        repository.addStat(letter.login, letter.sendDate.toDay(timezone), letter.openDate.toDay(timezone))
        LOG.info { "added" }
    }

    fun getLoginsWithUnorderedStatsSequence(): Sequence<String> {
        LOG.info { "getting sequence of all logins with unordered stats" }
        return repository.iterateLoginsWithUnorderedStats().iterator().asSequence()
    }

    fun orderStats(login: String) {
        transactionWrapper.executeInTransaction {
            val stats = loadStatData(login)
            val sent = orderStat(stats.sentStat, stats.unorderedSent)
            val open = orderStat(stats.openStat, stats.unorderedOpen)
            repository.setStatData(login, sent, open)
        }
    }

    private fun loadStatData(login: String): LetterStatData {
        LOG.info { "getting letter stat data for user=$login" }
        return repository.getStatData(login) ?: throw DatabaseException("user=$login not found")
    }

    private fun orderStat(stat: List<LetterStat>, unordered: List<Day>): List<LetterStat> {
        if (unordered.isEmpty()) return stat
        val statMap = HashMap<Day, Int>()
        stat.associateTo(statMap) { it.date to it.amount }
        unordered.forEach { statMap.merge(it, 1, Int::plus) }
        return statMap.map { LetterStat(it.key, it.value) }
    }

    private fun Date.toDay(timezone: String): Day {
        val zone = ZoneId.of(timezone)
        val localDate = this.toInstant().atZone(zone).toLocalDate()
        return Day(localDate.year.toShort(), localDate.monthValue.toByte(), localDate.dayOfMonth.toByte())
    }
}

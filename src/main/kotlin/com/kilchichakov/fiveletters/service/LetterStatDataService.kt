package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.repository.LetterStatDataRepository
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.Date


@Service
class LetterStatDataService(
        private val repository: LetterStatDataRepository,
        private val transactionWrapper: TransactionWrapper,
        private val letterService: LetterService
) {

    fun recalculateStatData(login: String) {
        LOG.info { "recalculate letter stat data for user=$login" }
        val letters = letterService.getLettersDatesSequence(login)
        val sentMap = HashMap<Day, Int>()
        val openMap = HashMap<Day, Int>()
        letters.forEach {
            val sentDay = it.sendDate.toDay()
            val openDay = it.openDate.toDay()
            sentMap.merge(sentDay, 1, Int::plus)
            openMap.merge(openDay, 1, Int::plus)
        }
        val sentStats = sentMap.map { LetterStat(it.key, it.value) }
        val openStats = openMap.map { LetterStat(it.key, it.value) }

        repository.setStatData(login, sentStats, openStats)
        LOG.info { "recalculation done"}
    }

    private fun Date.toDay(zone: ZoneId = ZoneId.of("UTC")): Day {
        val localDate = this.toInstant().atZone(zone).toLocalDate()
        return Day(localDate.year.toShort(), localDate.monthValue.toByte(), localDate.dayOfMonth.toByte())
    }
}

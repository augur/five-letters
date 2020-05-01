package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat
import com.kilchichakov.fiveletters.model.LetterStatData
import com.kilchichakov.fiveletters.service.findOneInTransaction
import com.kilchichakov.fiveletters.service.updateOneInTransaction
import com.mongodb.client.MongoDatabase
import org.bson.conversions.Bson
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.push
import org.litote.kmongo.setValue
import org.springframework.stereotype.Repository

@Repository
class LetterStatDataRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<LetterStatData>("userData")

    fun getStatData(login: String): LetterStatData? {
        LOG.info { "loading letterStatData of $login" }
        return collection.findOneInTransaction(LetterStatData::login eq login, false)
                .also { LOG.info { "found letterStatData $it" } }
    }

    fun setStatData(login: String, sentStats: List<LetterStat>, openStats: List<LetterStat>): Boolean {
        LOG.info { "Setting Letter Stat Data for login=$login, sent=$sentStats, open=$openStats" }

        val update = and(
                setValue(LetterStatData::openStat, prepare(openStats)),
                setValue(LetterStatData::sentStat, prepare(sentStats)),
                setValue(LetterStatData::unorderedOpen, emptyList()),
                setValue(LetterStatData::unorderedSent, emptyList())
        )
        val byLogin = LetterStatData::login eq login
        val result = collection.updateOneInTransaction(byLogin, update, false)
        LOG.info { "updated ${result.modifiedCount} Letter Stats" }
        return result.modifiedCount == 1L
    }

    fun addStat(login: String, sent: Day, open: Day): Boolean {
        LOG.info { "saving letter stat for user=$login, sent=$sent, open=$open" }
        val byLogin = LetterStatData::login eq login
        val update: Bson = and(
                push(LetterStatData::unorderedSent, sent),
                push(LetterStatData::unorderedOpen, open)
        )
        val result = collection.updateOneInTransaction(byLogin, update, false)
        LOG.info { "updated ${result.modifiedCount} Letter Stats" }
        return result.modifiedCount == 1L
    }

    private fun prepare(stats: List<LetterStat>): List<LetterStat> {
        val sorted = stats.sortedBy { it.date }
        for (i in 0 until stats.lastIndex) {
            if (sorted[i].date == sorted[i+1].date)
                throw DataException("stats=$stats contain duplicate date=${sorted[i].date}")
        }
        return sorted
    }
}
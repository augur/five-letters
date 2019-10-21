package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.exception.DataException
import com.kilchichakov.fiveletters.model.TimePeriod
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.and
import org.litote.kmongo.descending
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.orderBy
import org.springframework.stereotype.Repository

@Repository
class TimePeriodRepository(
        db: MongoDatabase
) {

    private val collection = db.getCollection<TimePeriod>()

    fun getTimePeriod(name: String): TimePeriod {
        LOG.info { "getting period $name" }
        val byId = TimePeriod::_id eq name
        val byEnabled = TimePeriod::enabled eq true
        val filter = and(byId, byEnabled)
        return collection.findOne(filter) ?: throw DataException("TimePeriod with name $name not found")
    }

    fun listTimePeriods(): List<String> {
        LOG.info { "getting all timePeriods" }
        val byEnabled = TimePeriod::enabled eq true
        val sorted = orderBy(listOf(TimePeriod::years, TimePeriod::months, TimePeriod::weeks, TimePeriod::days), true)

        return collection
                .find(byEnabled)
                .sort(sorted)
                .toList()
                .map { it._id }
    }
}
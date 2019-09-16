package com.kilchichakov.fiveletters.repository

import com.kilchichakov.fiveletters.LOG
import com.kilchichakov.fiveletters.model.DEFAULT_STATE
import com.kilchichakov.fiveletters.model.SystemState
import com.mongodb.client.MongoDatabase
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOne
import org.springframework.stereotype.Repository

@Repository
class SystemStateRepository(
        db: MongoDatabase
) {
    private val collection = db.getCollection<SystemState>()

    fun read(): SystemState {
        LOG.info { "reading systemState" }
        collection.findOne().let {
            if (it == null) {
                LOG.info { "systemState is null, returning $DEFAULT_STATE" }
                collection.insertOne(DEFAULT_STATE)
                return DEFAULT_STATE
            }
            LOG.info { "found systemState: $it" }
            return it
        }
    }

    fun switchRegistration(enable: Boolean) {
        LOG.info { "switching registration to $enable" }
        val filter = BsonDocument.parse("{}")
        val update = setValue(SystemState::registrationEnabled, enable)
        collection.updateOne(filter, update)
        LOG.info { "switched" }
    }
}
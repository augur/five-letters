package com.kilchichakov.fiveletters.repository

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
        collection.findOne().let {
            if (it == null) {
                collection.insertOne(DEFAULT_STATE)
                return DEFAULT_STATE
            }
            return it
        }
    }

    fun switchRegistration(enable: Boolean) {
        val filter = BsonDocument.parse("{}")
        val update = setValue(SystemState::registrationEnabled, enable)
        collection.updateOne(filter, update)
    }
}
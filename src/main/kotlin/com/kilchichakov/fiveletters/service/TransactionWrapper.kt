package com.kilchichakov.fiveletters.service

import com.kilchichakov.fiveletters.exception.DatabaseException
import com.mongodb.MongoClient
import com.mongodb.client.ClientSession
import com.mongodb.client.MongoCollection
import com.mongodb.client.result.UpdateResult
import org.bson.conversions.Bson
import org.litote.kmongo.findOne
import org.springframework.stereotype.Service
import java.lang.Exception

private val threadLocalSession = ThreadLocal<ClientSession>()

@Service
class TransactionWrapper(
        private val client: MongoClient
) {

    fun executeInTransaction(block:(session: ClientSession) -> Unit) {
        client.startSession().use {
            threadLocalSession.set(it)
            try {
                it.startTransaction()
                block(it)
                it.commitTransaction()
            } catch (e: Exception) {
                it.abortTransaction()
                throw e
            } finally {
                threadLocalSession.remove()
            }
        }
    }
}

fun MongoCollection<*>.updateOneInTransaction(filter: Bson, update: Bson, mandatory: Boolean): UpdateResult {
    val session = threadLocalSession.get()
    return if (session == null) {
        if (mandatory) throw DatabaseException("Attempted to run updateOne filter=$filter, update=$update w/o transaction")
        this.updateOne(filter, update)
    } else {
        this.updateOne(session, filter, update)
    }
}

fun <T>MongoCollection<T>.findOneInTransaction(filter: Bson, mandatory: Boolean): T? {
    val session = threadLocalSession.get()
    return if (session == null) {
        if (mandatory) throw DatabaseException("Attempted to run findOne filter=$filter, w/o transaction")
        this.findOne(filter)
    } else {
        this.findOne(session, filter)
    }
}

fun <T>MongoCollection<T>.insertOneInTransaction(document: T, mandatory: Boolean) {
    val session = threadLocalSession.get()
    return if (session == null) {
        if (mandatory) throw DatabaseException("Attempted to run insertOne document=$document, w/o transaction")
        this.insertOne(document)
    } else {
        this.insertOne(session, document)
    }
}
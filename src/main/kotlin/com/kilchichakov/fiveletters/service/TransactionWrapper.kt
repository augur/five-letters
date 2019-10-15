package com.kilchichakov.fiveletters.service

import com.mongodb.MongoClient
import com.mongodb.client.ClientSession
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class TransactionWrapper(
        private val client: MongoClient
) {

    fun executeInTransaction(block:(session: ClientSession) -> Unit) {
        client.startSession().use {
            try {
                it.startTransaction()
                block(it)
                it.commitTransaction()
            } catch (e: Exception) {
                it.abortTransaction()
                throw e
            }
        }
    }
}
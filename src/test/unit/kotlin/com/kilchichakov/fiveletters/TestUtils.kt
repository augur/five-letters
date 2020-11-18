package com.kilchichakov.fiveletters

import com.kilchichakov.fiveletters.service.TransactionWrapper
import com.mongodb.client.ClientSession
import io.mockk.every
import io.mockk.mockk
import org.litote.kmongo.setTo
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Any.invokePrivate(methodName: String, vararg arguments: Any?): Any? {
    val method = this::class.members.find { it.name == methodName }!!
    method.isAccessible = true
    return method.call(this, *arguments)
}

fun Any.setPrivate(fieldName: String, value: Any) {
    val property = this::class.memberProperties.find { it.name == fieldName }!!
    property.isAccessible = true
    property.setTo(value)
}

fun setUpTransactionWrapperMock(wrapper: TransactionWrapper, session: ClientSession? = null) {
    every { wrapper.executeInTransaction(any()) } answers {
        firstArg<(ClientSession)->Any>().invoke(session ?: mockk())
    }
}

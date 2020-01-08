package com.kilchichakov.fiveletters

import org.litote.kmongo.setTo
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

fun Any.invokePrivate(methodName: String, vararg arguments: Any): Any? {
    val method = this::class.members.find { it.name == methodName }!!
    method.isAccessible = true
    return method.call(this, *arguments)
}

fun Any.setPrivate(fieldName: String, value: Any) {
    val property = this::class.memberProperties.find { it.name == fieldName }!!
    property.isAccessible = true
    property.setTo(value)
}
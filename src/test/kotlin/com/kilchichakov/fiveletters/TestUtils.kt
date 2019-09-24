package com.kilchichakov.fiveletters

import kotlin.reflect.jvm.isAccessible

fun Any.invokePrivate(methodName: String, vararg arguments: Any): Any? {
    val method = this::class.members.find { it.name == methodName }!!
    method.isAccessible = true
    return method.call(this, *arguments)
}
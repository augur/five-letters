package com.kilchichakov.fiveletters.model

data class Page<T>(
    val elements: List<T>,
    val number: Int,
    val size: Int,
    val total: Int
)
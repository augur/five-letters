package com.kilchichakov.fiveletters.model

data class Page<T>(
        val elements: List<T>,
        val pageNumber: Int,
        val pageSize: Int,
        val total: Int
)
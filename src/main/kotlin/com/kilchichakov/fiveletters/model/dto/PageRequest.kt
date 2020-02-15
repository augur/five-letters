package com.kilchichakov.fiveletters.model.dto

data class PageRequest(
        val pageNumber: Int,
        val pageSize: Int,
        val includeRead: Boolean,
        val includeMailed: Boolean,
        val includeArchived: Boolean,
        val sortBy: String? = null
)
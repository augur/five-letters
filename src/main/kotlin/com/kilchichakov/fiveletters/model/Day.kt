package com.kilchichakov.fiveletters.model

data class Day(
        val year: Short,
        val month: Byte,
        val day: Byte
) : Comparable<Day> {

    override fun compareTo(other: Day): Int = when {
        year != other.year -> year - other.year
        month != other.month -> month - other.month
        else -> day - other.day
    }
}
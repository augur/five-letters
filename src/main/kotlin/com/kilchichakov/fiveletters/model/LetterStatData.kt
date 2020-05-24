package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId

data class LetterStatData(
        val _id: ObjectId?,
        val login: String,
        val sentStat: List<LetterStat> = emptyList(),
        val openStat: List<LetterStat> = emptyList(),
        val unorderedSent: List<Day> = emptyList(),
        val unorderedOpen: List<Day> = emptyList()
)
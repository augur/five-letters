package com.kilchichakov.fiveletters.model

import org.bson.types.ObjectId

data class LetterStatData(
        val _id: ObjectId?,
        val login: String,
        val sentStat: List<LetterStat>,
        val openStat: List<LetterStat>,
        val unorderedSent: List<Day>,
        val unorderedOpen: List<Day>
)
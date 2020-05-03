package com.kilchichakov.fiveletters.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.kilchichakov.fiveletters.service.serde.LetterStatDeserializer
import com.kilchichakov.fiveletters.service.serde.LetterStatSerializer

@JsonSerialize(using = LetterStatSerializer::class)
@JsonDeserialize(using = LetterStatDeserializer::class)
data class LetterStat(
        val date: Day,
        val amount: Int
)
package com.kilchichakov.fiveletters.service.serde

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.kilchichakov.fiveletters.model.Day
import com.kilchichakov.fiveletters.model.LetterStat

class LetterStatDeserializer : StdDeserializer<LetterStat>(LetterStat::class.java) {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LetterStat {
        p as JsonParser
        val s= p.readValueAs(String::class.java)
        val year = s.substring(0, 4).toShort()
        val month = s.substring(4, 6).toByte()
        val day = s.substring(6, 8).toByte()
        val amount = s.substring(8).toInt()
        return LetterStat(Day(year, month, day), amount)
    }
}
package com.kilchichakov.fiveletters.service.serde

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.kilchichakov.fiveletters.model.LetterStat

class LetterStatSerializer : StdSerializer<LetterStat>(LetterStat::class.java) {

    override fun serialize(value: LetterStat?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen as JsonGenerator
        value as LetterStat
        provider as SerializerProvider
        gen.writeString(String.format("%04d%02d%02d%d", value.date.year, value.date.month, value.date.day, value.amount))
    }

    override fun acceptJsonFormatVisitor(visitor: JsonFormatVisitorWrapper?, typeHint: JavaType?) {
        visitor?.expectStringFormat(typeHint)
    }
}
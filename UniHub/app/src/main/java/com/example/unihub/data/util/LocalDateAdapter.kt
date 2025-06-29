package com.example.unihub.data.util

import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate

/**
 * Gson adapter to serialize and deserialize [LocalDate] values using ISO-8601 format.
 */
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src == null) JsonPrimitive(null as String?) else JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalDate {
        return json?.asString?.let { LocalDate.parse(it) } ?: throw IllegalStateException("LocalDate value is null")
    }
}
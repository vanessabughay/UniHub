package com.example.unihub.data.util

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

private fun parseFlexibleLong(value: String): Long {
    val cleaned = value.trim()
    if (cleaned.isEmpty()) {
        throw JsonParseException("Valor vazio não pode ser convertido em Long")
    }

    cleaned.toLongOrNull()?.let { return it }

    return try {
        Instant.parse(cleaned).toEpochMilli()
    } catch (instantEx: DateTimeParseException) {
        try {
            OffsetDateTime.parse(cleaned).toInstant().toEpochMilli()
        } catch (offsetEx: DateTimeParseException) {
            try {

                LocalDateTime.parse(cleaned)

                    .atZone(ZoneId.systemDefault())

                    .toInstant()

                    .toEpochMilli()

            } catch (localDateTimeEx: DateTimeParseException) {

                try {

                    LocalDate.parse(cleaned)

                        .atStartOfDay(ZoneId.systemDefault())

                        .toInstant()

                        .toEpochMilli()

                } catch (localDateEx: DateTimeParseException) {

                    throw JsonParseException(

                        "Não foi possível converter '$value' em Long",

                        localDateEx

                    )

                }

            }
        }
    }
}

class FlexibleLongAdapter : TypeAdapter<Long>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Long {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                0L
            }
            JsonToken.NUMBER -> reader.nextDouble().toLong()
            JsonToken.STRING -> parseFlexibleLong(reader.nextString())
            else -> throw JsonParseException("Token JSON inesperado para Long: ${reader.peek()}")
        }
    }
}

class FlexibleNullableLongAdapter : TypeAdapter<Long?>() {
    override fun write(out: JsonWriter, value: Long?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Long? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.NUMBER -> reader.nextDouble().toLong()
            JsonToken.STRING -> parseFlexibleLong(reader.nextString())
            else -> throw JsonParseException("Token JSON inesperado para Long?: ${reader.peek()}")
        }
    }
}
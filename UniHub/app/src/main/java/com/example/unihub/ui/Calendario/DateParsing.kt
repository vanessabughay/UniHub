package com.example.unihub.ui.Calendario

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//converter os dados de avaliação em localdate
fun parseToLocalDate(value: String?): LocalDate? {
    if (value.isNullOrBlank()) return null

    // Tente ISO LocalDate
    runCatching { return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE) }

    // Tente ISO LocalDateTime
    runCatching { return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate() }

    // dd/MM/yyyy
    runCatching {
        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return LocalDate.parse(value, fmt)
    }

    // yyyy-MM-dd HH:mm:ss
    runCatching {
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDate.parse(value.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE)
    }

    return null
}
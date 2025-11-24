package com.example.unihub.ui.Shared

import java.util.Locale
import kotlin.math.roundToInt

object NotaCampo {
    fun formatFieldText(text: String): String = sanitize(text)

    fun sanitize(raw: String): String {
        if (raw.isBlank()) return ""

        val normalized = raw.replace('.', ',')
        val result = StringBuilder()
        var hasComma = false

        for (ch in normalized) {
            when {
                ch.isDigit() -> result.append(ch)
                ch == ',' && !hasComma -> {
                    hasComma = true
                    if (result.isEmpty()) result.append('0')
                    result.append(',')
                }
            }
        }

        return result.toString()
    }

    fun fromDouble(nota: Double?): String {
        return nota?.let { formatNumero(it) } ?: ""
    }

    fun toDouble(valor: String): Double? {
        val sanitized = sanitize(valor)
        if (sanitized.isEmpty() || sanitized.last() == ',') return null
        val normalized = sanitized.replace(',', '.')
        return normalized.toDoubleOrNull()
    }

    fun formatListValue(nota: Double?): String {
        return nota?.let { formatNumero(it) } ?: "-"
    }

    private fun formatNumero(v: Double): String {
        val s = String.format(Locale.US, "%.1f", v)
        return s.replace('.', ',')
    }
}

object PesoCampo {
    fun sanitize(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return ""

        val trimmed = digitsOnly.trimStart('0')
        val normalized = when {
            trimmed.isEmpty() -> "0"
            else -> trimmed
        }

        val limited = normalized.take(3)
        val valor = limited.toIntOrNull() ?: return ""

        return when {
            valor > 100 -> "100"
            else -> valor.toString()
        }
    }

    fun fromDouble(peso: Double?): String {
        return peso?.let { sanitize(formatInteiro(it)) } ?: ""
    }

    fun toDouble(valor: String): Double? {
        return valor.toIntOrNull()?.toDouble()
    }

    fun formatListValue(peso: Double?): String {
        return peso?.let { "${formatInteiro(it)}%" } ?: "-"
    }

    fun formatTotal(valor: Double): String {
        return "${formatInteiro(valor)}%"
    }

    private fun formatInteiro(v: Double): String = v.roundToInt().toString()
}
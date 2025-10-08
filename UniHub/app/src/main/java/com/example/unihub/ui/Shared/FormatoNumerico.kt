package com.example.unihub.ui.Shared

import java.util.Locale
import kotlin.math.roundToInt

object NotaCampo {
    fun formatFieldText(digits: String): String {
        if (digits.isEmpty()) return ""
        val integerPartRaw = if (digits.length == 1) "" else digits.dropLast(1)
        val integerPart = integerPartRaw.trimStart('0').ifEmpty { "0" }
        val decimalPart = digits.last().toString()
        return "$integerPart,$decimalPart"
    }

    fun sanitize(raw: String): String {
        val digitsOnly = raw.filter { it.isDigit() }
        return when {
            digitsOnly.isEmpty() -> ""
            digitsOnly.length == 1 -> digitsOnly
            else -> digitsOnly.trimStart('0').ifEmpty { "0" }
        }
    }

    fun fromDouble(nota: Double?): String {
        return nota?.let { toDigits(it) } ?: ""
    }

    fun toDouble(digits: String): Double? {
        if (digits.isEmpty()) return null
        val inteiro = digits.toLongOrNull() ?: return null
        return inteiro / 10.0
    }

    fun formatListValue(nota: Double?): String {
        return nota?.let { formatNumero(it) } ?: "-"
    }

    private fun toDigits(nota: Double): String {
        val valorEscalado = (nota * 10).roundToInt()
        return valorEscalado.toString()
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
package com.example.unihub.data.model

import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.unihub.data.util.FlexibleLongAdapter
import com.example.unihub.data.util.FlexibleNullableLongAdapter
import com.google.gson.annotations.JsonAdapter

// Função auxiliar para obter o timestamp de hoje (início do dia) como padrão
private fun getDefaultPrazo(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

data class Tarefa(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val status: Status = Status.INICIADA,
    val responsaveisIds: List<Long> = emptyList(),
    @JsonAdapter(FlexibleLongAdapter::class)
    val prazo: Long = getDefaultPrazo(), // PRAZO AGORA É NÃO-NULLABLE e tem um padrão
    @JsonAdapter(FlexibleLongAdapter::class)
    val dataInicio: Long = System.currentTimeMillis(),
    @JsonAdapter(FlexibleNullableLongAdapter::class)
    val dataFim: Long? = null
) {
    val duracao: String
        get() {
            return if (dataFim != null) {
                val diff = dataFim - dataInicio
                if (diff >= 0) {
                    formatarDuracao(diff)
                } else {
                    "Inválida"
                }
            } else {
                "N/A"
            }
        }

    private fun formatarDuracao(millis: Long): String {
        if (millis == 0L) return "0 minutos"

        val umMinutoEmMs = TimeUnit.MINUTES.toMillis(1)
        val umaHoraEmMs = TimeUnit.HOURS.toMillis(1)
        val umDiaEmMs = TimeUnit.DAYS.toMillis(1)
        val umMesEmMs = umDiaEmMs * 30 // Aproximação

        return when {
            millis >= umMesEmMs -> {
                val meses = millis / umMesEmMs
                if (meses == 1L) "$meses mês" else "$meses meses"
            }
            millis >= umDiaEmMs -> {
                val dias = millis / umDiaEmMs
                if (dias == 1L) "$dias dia" else "$dias dias"
            }
            millis >= umaHoraEmMs -> {
                val horas = millis / umaHoraEmMs
                if (horas == 1L) "$horas hora" else "$horas horas"
            }
            millis >= umMinutoEmMs -> {
                val minutos = millis / umMinutoEmMs
                if (minutos == 1L) "$minutos minuto" else "$minutos minutos"
            }
            millis > 0 -> "Menos de 1 minuto"
            else -> "Inválida"
        }
    }
}
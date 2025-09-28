package com.example.unihub.data.model

import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.model.Priority
import com.example.unihub.data.util.FlexibleLongAdapter
import com.example.unihub.data.util.FlexibleNullableLongAdapter
import com.google.gson.annotations.JsonAdapter
import java.util.concurrent.TimeUnit

data class Coluna(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val prioridade: Priority = Priority.MEDIA,
    val status: Status = Status.INICIADA,
    @JsonAdapter(FlexibleLongAdapter::class)
    val dataInicio: Long = System.currentTimeMillis(),
    @JsonAdapter(FlexibleNullableLongAdapter::class)
    val dataFim: Long? = null,
    @JsonAdapter(FlexibleLongAdapter::class)
    val prazoManual: Long = System.currentTimeMillis() + 86400000L,
    val tarefas: List<Tarefa> = emptyList()
) {

    val todasTarefasConcluidas: Boolean
        get() = tarefas.isNotEmpty() && tarefas.all { it.status == Status.CONCLUIDA }

    // Duração formatada
    val duracao: String
        get() {
            if (tarefas.isEmpty()) {
                return if (dataFim != null) {
                    formatarDuracao(dataFim - dataInicio)
                } else {
                    "N/A"
                }
            } else {
                if (todasTarefasConcluidas && status == Status.CONCLUIDA) {
                    val ultimaDataFimTarefa = tarefas
                        .filter { it.status == Status.CONCLUIDA && it.dataFim != null }
                        .maxOfOrNull { it.dataFim!! }

                    return if (ultimaDataFimTarefa != null) {
                        formatarDuracao(ultimaDataFimTarefa - dataInicio)
                    } else {
                        "N/A"
                    }
                } else {
                    return "N/A"
                }
            }
        }

    // Prazo calculado com base nas tarefas (ou manual se não tiver tarefas)
    val prazoCalculado: Long
        get() = if (tarefas.isNotEmpty()) {
            // MUDANÇA: A lógica foi simplificada
            tarefas.maxOfOrNull { it.prazo } ?: prazoManual
        } else {
            prazoManual
        }

    private fun formatarDuracao(millis: Long): String {
        if (millis < 0) return "Inválida"

        val umMinutoEmMs = TimeUnit.MINUTES.toMillis(1)
        val umaHoraEmMs = TimeUnit.HOURS.toMillis(1)
        val umDiaEmMs = TimeUnit.DAYS.toMillis(1)
        val umMesEmMs = umDiaEmMs * 30

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
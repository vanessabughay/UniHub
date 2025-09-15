package com.example.unihub.data.model

import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import java.util.concurrent.TimeUnit

data class Coluna(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val prioridade: Priority = Priority.MEDIA,
    val status: Status = Status.INICIADA,
    val dataInicio: Long = System.currentTimeMillis(),
    val dataFim: Long? = null,
    val prazoManual: Long = System.currentTimeMillis() + 86400000L,
    val tarefas: List<Tarefa> = emptyList()
) {
   
    val todasTarefasConcluidas: Boolean
        get() = tarefas.isNotEmpty() && tarefas.all { it.status == Status.CONCLUIDA }

    // Duração formatada
    val duracao: String
        get() {
            // Se a Coluna não tem tarefas, sua duração é baseada em seu próprio dataFim
            if (tarefas.isEmpty()) {
                return if (dataFim != null) {
                    formatarDuracao(dataFim - dataInicio)
                } else {
                    "N/A"
                }
            } else {
                // Se tem tarefas, a duração só é calculada se TODAS as tarefas estiverem concluídas
                if (todasTarefasConcluidas && status == Status.CONCLUIDA) {
                    // Encontra a data de fim mais recente entre as tarefas concluídas
                    val ultimaDataFimTarefa = tarefas
                        .filter { it.status == Status.CONCLUIDA && it.dataFim != null }
                        .maxOfOrNull { it.dataFim!! }

                    return if (ultimaDataFimTarefa != null) {
                        formatarDuracao(ultimaDataFimTarefa - dataInicio)
                    } else {
                        "N/A"
                    }
                } else {
                    // Se nem todas as tarefas estão concluídas ou a Coluna principal não está CONCLUIDA
                    return "N/A"
                }
            }
        }

    // Prazo calculado com base nas tarefas (ou manual se não tiver tarefas)
    val prazoCalculado: Long
        get() = if (tarefas.isNotEmpty()) {
            // Pega o maior prazo entre as tarefas. Se nenhuma tiver prazo (null), usa o prazoManual da Coluna
            tarefas.maxOfOrNull { it.prazo ?: 0L } ?: prazoManual
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
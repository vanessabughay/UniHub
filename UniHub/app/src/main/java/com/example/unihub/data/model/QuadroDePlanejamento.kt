package com.example.unihub.data.model

import com.example.unihub.data.model.Coluna
import com.example.unihub.data.model.Status
import java.util.concurrent.TimeUnit

// Enum para o estado do quadro
enum class Estado {
    ATIVO,
    INATIVO
}

data class QuadroDePlanejamento(
    val id: String = "",
    val nome: String = "",
    val disciplina: String? = null,
    val integrantes: List<String>? = null,
    val estado: Estado = Estado.ATIVO,
    val colunas: List<Coluna> = emptyList(),
    val dataInicio: Long = System.currentTimeMillis(),
    val dataFim: Long? = null
) {
    val prazoCalculado: Long
        get() = if (colunas.isNotEmpty()) {
            colunas.maxOfOrNull { it.prazoCalculado } ?: 0L
        } else {
            0L
        }

    val duracao: String
        get() {
            if (colunas.isEmpty()) {
                return "N/A"
            }

            val ultimaDataFimColuna = colunas
                .filter { it.status == Status.CONCLUIDA && it.dataFim != null }
                .maxOfOrNull { it.dataFim!! }

            return if (ultimaDataFimColuna != null) {
                // A lógica de dataInicio agora funcionará corretamente
                formatarDuracao(ultimaDataFimColuna - dataInicio)
            } else {
                "N/A"
            }
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
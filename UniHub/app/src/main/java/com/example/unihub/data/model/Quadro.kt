package com.example.unihub.data.model

import java.util.concurrent.TimeUnit
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.example.unihub.data.util.FlexibleNullableLongAdapter


// Enum para o estado do quadro
enum class Estado {
    @SerializedName("ATIVO")
    ATIVO,
    @SerializedName("ENCERRADO")
    INATIVO
}

data class Quadro(
    val id: String? = null,
    val nome: String = "",

    //--- campos incluidos
    val disciplinaId: Long? = null,
    val contatoId: Long? = null,
    val grupoId: Long? = null,

    val estado: Estado = Estado.ATIVO,
    val colunas: List<Coluna> = emptyList(),
    @JsonAdapter(FlexibleNullableLongAdapter::class)
    val dataFim: Long? = null,
    val donoId: Long? = null
) {

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
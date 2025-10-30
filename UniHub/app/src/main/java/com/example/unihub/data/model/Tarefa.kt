package com.example.unihub.data.model

import android.os.Parcelable
import java.util.Calendar
import com.example.unihub.data.util.FlexibleLongAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// Função auxiliar para obter o timestamp de hoje (início do dia) como padrão
private fun getDefaultPrazo(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Parcelize
data class Tarefa(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val status: Status = Status.INICIADA,
    @SerializedName("responsavelIds")
    val responsaveisIds: List<Long> = emptyList(),
    @JsonAdapter(FlexibleLongAdapter::class)
    val prazo: Long = getDefaultPrazo() // PRAZO AGORA É NÃO-NULLABLE e tem um padrão
) : Parcelable {

}
package com.example.unihub.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tarefa(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val status: Status = Status.INICIADA,
    @SerializedName(value = "responsaveisIds", alternate = ["responsavelIds"])
    val responsaveisIds: List<Long> = emptyList(),
    @SerializedName(value = "dataPrazo", alternate = ["prazo"])
    val prazo: String? = null,
) : Parcelable {

}
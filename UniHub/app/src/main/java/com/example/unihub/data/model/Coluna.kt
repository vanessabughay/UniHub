package com.example.unihub.data.model

import com.example.unihub.data.model.Status
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.util.FlexibleLongAdapter
import com.example.unihub.data.util.FlexibleNullableLongAdapter
import com.google.gson.annotations.JsonAdapter
import java.util.concurrent.TimeUnit

data class Coluna(
    val id: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val status: Status = Status.INICIADA,
    val ordem: Int = 0,
    val tarefas: List<Tarefa> = emptyList()
) {

    val todasTarefasConcluidas: Boolean
        get() = tarefas.isNotEmpty() && tarefas.all { it.status == Status.CONCLUIDA }

}
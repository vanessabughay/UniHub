package com.example.unihub.data.repository

import com.example.unihub.data.model.Tarefa
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TarefaApi {

    @GET("colunas/{colunaId}/tarefas/{tarefaId}")
    suspend fun getTarefa(
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    ): Tarefa

    @POST("colunas/{colunaId}/tarefas")
    suspend fun createTarefa(
        @Path("colunaId") colunaId: String,
        @Body tarefa: Tarefa
    ): Tarefa

    @PUT("colunas/{colunaId}/tarefas/{tarefaId}")
    suspend fun updateTarefa(
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Body tarefa: Tarefa
    ): Tarefa

    @DELETE("colunas/{colunaId}/tarefas/{tarefaId}")
    suspend fun deleteTarefa(
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    )
}
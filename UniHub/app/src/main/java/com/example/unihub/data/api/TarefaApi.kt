package com.example.unihub.data.api

import com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
import com.example.unihub.data.model.Tarefa
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TarefaApi {

    @GET("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}")
    suspend fun getTarefa(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    ): Tarefa

    @POST("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas")
    suspend fun createTarefa(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Body tarefa: TarefaPlanejamentoRequestDto
    )


    @PUT("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}")
    suspend fun updateTarefa(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Body tarefa: Tarefa
    ): Tarefa

        suspend fun deleteTarefa(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    )
}
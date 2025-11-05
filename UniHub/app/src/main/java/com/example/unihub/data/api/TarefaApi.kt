package com.example.unihub.data.api

import com.example.unihub.data.dto.AtualizarTarefaPlanejamentoRequestDto
import com.example.unihub.data.dto.TarefaNotificacaoRequestDto
import com.example.unihub.data.dto.ComentarioRequestDto
import com.example.unihub.data.dto.TarefaPlanejamentoRequestDto
import com.example.unihub.data.model.Tarefa
import com.example.unihub.data.model.Comentario
import com.example.unihub.data.model.TarefaPreferenciaResponse
import com.example.unihub.data.model.ComentariosResponse
import com.example.unihub.data.dto.TarefaDto

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
        @Body tarefa: AtualizarTarefaPlanejamentoRequestDto
    ): Tarefa

    @DELETE("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}")
        suspend fun deleteTarefa(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    )

    @GET("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios")
    suspend fun getComentarios(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String
    ): ComentariosResponse

    @POST("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios")
    suspend fun createComentario(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Body comentario: ComentarioRequestDto
    ): Comentario

    @PUT("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios/{comentarioId}")
    suspend fun updateComentario(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Path("comentarioId") comentarioId: String,
        @Body comentario: ComentarioRequestDto
    ): Comentario

    @DELETE("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}/comentarios/{comentarioId}")
    suspend fun deleteComentario(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Path("comentarioId") comentarioId: String
    )

    @PUT("quadros-planejamento/{quadroId}/colunas/{colunaId}/tarefas/{tarefaId}/preferencias")
    suspend fun updateTarefaPreference(
        @Path("quadroId") quadroId: String,
        @Path("colunaId") colunaId: String,
        @Path("tarefaId") tarefaId: String,
        @Body request: TarefaNotificacaoRequestDto
    ): TarefaPreferenciaResponse

    @GET("quadros-planejamento/tarefas/proximas")
    suspend fun getProximasTarefas(): List<TarefaDto>
}
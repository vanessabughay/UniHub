// com.example.unihub.data.remote
package com.example.unihub.data.remote

import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula
import retrofit2.Response
import retrofit2.http.*

// Defina DisciplinaResumo aqui ou importe se estiver em outro arquivo
data class DisciplinaResumo(
    val id: String,
    val nome: String,
    val aulas: List<HorarioAula>
)

interface DisciplinaApiService {
    @GET("api/disciplinas/resumo")
    suspend fun getDisciplinasResumoApi(): Response<List<DisciplinaResumo>>

    @GET("api/disciplinas/{id}")
    suspend fun getDisciplinaByIdApi(@Path("id") id: String): Response<Disciplina>

    @POST("api/disciplinas")
    suspend fun addDisciplinaApi(@Body disciplina: Disciplina): Response<Disciplina>

    @PUT("api/disciplinas/{id}")
    suspend fun updateDisciplinaApi(@Path("id") id: String, @Body disciplina: Disciplina): Response<Disciplina>

    @DELETE("api/disciplinas/{id}")
    suspend fun deleteDisciplinaApi(@Path("id") id: String): Response<Unit>
}
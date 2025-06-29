package com.example.unihub.data.remote

import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.HorarioAula // Mantenha se HorarioAula for usado em DisciplinaResumo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Query // Importar para @Query

// Se você tiver uma DisciplinaResumo data class em 'data.remote', ajuste-a também:
// (Se você a usa, o ID também deve ser Long?)
data class DisciplinaResumo(
    val id: Long?, // <<< ESTE DEVE SER Long?
    val nome: String,
    val aulas: List<HorarioAula>
)

interface DisciplinaApiService {
    // URL base é "disciplinas", e o backend retorna List<Disciplina>
    @GET("disciplinas")
    suspend fun getAllDisciplinas(): Response<List<Disciplina>>

    @GET("disciplinas/{id}")
    suspend fun getDisciplinaByIdApi(@Path("id") id: Long): Response<Disciplina> // <<< ESTE DEVE SER Long

    @POST("disciplinas")
    suspend fun createDisciplina(@Body disciplina: Disciplina): Response<Disciplina>

    @PUT("disciplinas/{id}")
    suspend fun updateDisciplina(@Path("id") id: Long, @Body disciplina: Disciplina): Response<Disciplina> // <<< ESTE DEVE SER Long

    @DELETE("disciplinas/{id}")
    suspend fun deleteDisciplina(@Path("id") id: Long): Response<Unit> // <<< ESTE DEVE SER Long

    @GET("disciplinas/pesquisa")
    suspend fun buscarDisciplinasPorNome(@Query("nome") nome: String): Response<List<Disciplina>>
}
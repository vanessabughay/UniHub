package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.DisciplinaApi
import com.example.unihub.data.api.QuadroApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.model.QuadroDePlanejamento
import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.repository.QuadroResumo
import com.example.unihub.data.repository._disciplinabackend
import com.example.unihub.data.repository._quadrobackend
import java.io.IOException
import kotlin.collections.map
import kotlin.collections.orEmpty

class ApiQuadroBackend : _quadrobackend {
    private val api: QuadroApi by lazy {
        RetrofitClient.create(QuadroApi::class.java)
    }

    override suspend fun getQuadrosResumoApi(): List<DisciplinaResumo> {
        val resp = api.list()
        if (!resp.isSuccessful) {
            throw IOException("Erro ao listar quadros: ${resp.code()} ${resp.errorBody()?.string()}")
        }
        val quadros = resp.body().orEmpty()
        return quadros.map { d ->
            // o id não é nulo (senão lança IllegalArgumentException)
            val id = requireNotNull(d.id) { "Quadro sem id do backend: $d" }

            QuadroResumo(
                id = id,
                nome = d.nome.orEmpty()
            )
        }
    }

    override suspend fun getQuadroByIdApi(id: String): Quadro? {
        val resp = api.get(id.toLong())
        return if (resp.isSuccessful) resp.body() else null
    }

    override suspend fun addQuadroApi(quadro: Quadro) {
        val resp = api.add(quadro)
        if (!resp.isSuccessful) {
            throw IOException("Erro ao adicionar quadro: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    override suspend fun updateQuadroApi(id: Long, quadro: Quadro): Boolean {
        val resp = api.update(id, quadro)
        return resp.isSuccessful
    }

    override suspend fun deleteQuadroApi(id: Long): Boolean {
        val resp = api.delete(id)
        return resp.isSuccessful
    }


}
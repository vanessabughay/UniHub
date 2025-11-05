package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.DisciplinaApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.repository.DisciplinaResumo
import com.example.unihub.data.repository._disciplinabackend
import java.io.IOException

class ApiDisciplinaBackend : _disciplinabackend {
    private val api: DisciplinaApi by lazy {
        RetrofitClient.create(DisciplinaApi::class.java)
    }

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> {
        val resp = api.list()
        if (!resp.isSuccessful) {
            throw IOException("Erro ao listar disciplinas: ${resp.code()} ${resp.errorBody()?.string()}")
        }
        val disciplinas = resp.body().orEmpty()
        return disciplinas.map { d ->
            // o id não é nulo (senão lança IllegalArgumentException)
            val id = requireNotNull(d.id) { "Disciplina sem id do backend: $d" }

            DisciplinaResumo(
                id = id,
                codigo = d.codigo,
                nome = d.nome.orEmpty(),
                aulas = d.aulas,
                receberNotificacoes = d.receberNotificacoes,
                totalAusencias = d.ausencias?.size ?: 0,
                ausenciasPermitidas = d.ausenciasPermitidas,
                isAtiva = d.isAtiva
            )
        }
    }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? {
        val resp = api.get(id.toLong())
        return if (resp.isSuccessful) resp.body() else null
    }

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        val resp = api.add(disciplina)
        if (!resp.isSuccessful) {
            throw IOException("Erro ao adicionar disciplina: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    override suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean {
        val resp = api.update(id, disciplina)
        return resp.isSuccessful
    }

    override suspend fun deleteDisciplinaApi(id: Long): Boolean {
        val resp = api.delete(id)
        return resp.isSuccessful
    }


}

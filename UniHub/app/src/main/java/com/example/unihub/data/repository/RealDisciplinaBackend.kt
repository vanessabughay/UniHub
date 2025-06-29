package com.example.unihub.data.repository

import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.remote.DisciplinaApiService

class RealDisciplinaBackend(
    private val api: DisciplinaApiService
) : _disciplinabackend {

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> {
        return api.getAll().map { disciplina ->
            DisciplinaResumo(
                id = disciplina.id?.toString() ?: "",
                nome = disciplina.nome,
                aulas = disciplina.aulas
            )
        }
    }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? {
        return api.getById(id.toLong())
    }

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        api.create(disciplina)
    }

    override suspend fun updateDisciplinaApi(disciplina: Disciplina): Boolean {
        val id = disciplina.id ?: return false
        api.update(id, disciplina)
        return true
    }

    override suspend fun deleteDisciplinaApi(id: String): Boolean {
        api.delete(id.toLong())
        return true
    }
}

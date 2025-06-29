package com.example.unihub.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.model.Disciplina

class ApiDisciplinaBackend : _disciplinabackend {
    private val api: DisciplinaApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")   // emulator loopback
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DisciplinaApi::class.java)
    }

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> =
        api.list().map { DisciplinaResumo(it.id.toString(), it.nome, it.aulas) }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? =
        api.get(id.toLong())

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        api.add(disciplina)
    }

    override suspend fun updateDisciplinaApi(disciplina: Disciplina): Boolean {
        api.update(disciplina.id.toLong(), disciplina)
        return true
    }

    override suspend fun deleteDisciplinaApi(id: String): Boolean {
        api.delete(id.toLong())
        return true
    }
}

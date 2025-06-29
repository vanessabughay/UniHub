package com.example.unihub.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.model.Disciplina
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import java.time.LocalDate

class ApiDisciplinaBackend : _disciplinabackend {
    private val api: DisciplinaApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")   // emulator loopback
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(DisciplinaApi::class.java)
    }

    override suspend fun getDisciplinasResumoApi(): List<DisciplinaResumo> =
        api.list().map { DisciplinaResumo(it.id!!,it.codigo, it.nome, it.aulas) }

    override suspend fun getDisciplinaByIdApi(id: String): Disciplina? =
        api.get(id.toLong())

    override suspend fun addDisciplinaApi(disciplina: Disciplina) {
        api.add(disciplina)
    }

    override suspend fun updateDisciplinaApi(id: Long, disciplina: Disciplina): Boolean {
        api.update(id, disciplina)
        return true
    }

    override suspend fun deleteDisciplinaApi(id: Long): Boolean {
        api.delete(id)
        return true
    }

}

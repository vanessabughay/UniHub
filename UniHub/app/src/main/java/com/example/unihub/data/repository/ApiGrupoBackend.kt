package com.example.unihub.data.repository

import com.example.unihub.data.model.Grupo
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate


class ApiGrupoBackend : Grupobackend {
    private val api: GrupoApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(GrupoApi::class.java)
    }


    override suspend fun getGrupoApi(): List<Grupo> {
        return api.list().map { Grupo(it.id!!, it.nome!!, it.membros!!) }
    }

    override suspend fun getGrupoByIdApi(id: String): Grupo? {
        return api.get(id.toLong())
    }

    override suspend fun addGrupoApi(grupo: Grupo) {
        api.add(grupo)
    }

    override suspend fun updateGrupoApi(id: Long, grupo: Grupo): Boolean {
        api.update(id, grupo)
        return true // Retorno booleano como na interface
    }

    override suspend fun deleteGrupoApi(id: Long): Boolean {
        api.delete(id)
        return true // Retorno booleano como na interface
    }
}

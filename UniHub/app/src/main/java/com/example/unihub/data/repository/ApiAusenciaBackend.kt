package com.example.unihub.data.repository

import com.example.unihub.data.model.Ausencia
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class ApiAusenciaBackend : _ausenciabackend {
    private val api: AusenciaApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AusenciaApi::class.java)
    }

    override suspend fun listAusenciasApi(): List<Ausencia> = api.list()

    override suspend fun getAusenciaByIdApi(id: Long): Ausencia? = api.get(id)

    override suspend fun addAusenciaApi(ausencia: Ausencia) {
        api.add(ausencia)
    }

    override suspend fun updateAusenciaApi(id: Long, ausencia: Ausencia): Boolean {
        api.update(id, ausencia)
        return true
    }

    override suspend fun deleteAusenciaApi(id: Long): Boolean {
        api.delete(id)
        return true
    }
}
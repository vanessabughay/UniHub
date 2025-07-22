package com.example.unihub.data.repository

import com.example.unihub.data.model.Categoria
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

class ApiCategoriaBackend : _categoriabackend {
    private val api: CategoriaApi by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CategoriaApi::class.java)
    }

    override suspend fun listCategoriasApi(): List<Categoria> = api.list()

    override suspend fun addCategoriaApi(categoria: Categoria) {
        api.add(categoria)
    }
}
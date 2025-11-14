package com.example.unihub.data.apiBackend

import com.example.unihub.data.api.CategoriaApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.model.Categoria
import com.example.unihub.data.repository._categoriabackend


class ApiCategoriaBackend : _categoriabackend {
    private val api: CategoriaApi by lazy {
        RetrofitClient.create(CategoriaApi::class.java)
    }

    override suspend fun listCategoriasApi(): List<Categoria> = api.list()

    override suspend fun addCategoriaApi(categoria: Categoria) {
        api.add(categoria)
    }
}
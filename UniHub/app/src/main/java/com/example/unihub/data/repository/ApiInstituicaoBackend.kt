package com.example.unihub.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.model.Instituicao
import com.example.unihub.BuildConfig

class ApiInstituicaoBackend : _instituicaobackend {
    private val api: InstituicaoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InstituicaoApi::class.java)
    }

    override suspend fun buscarInstituicoesApi(query: String): List<Instituicao> =
        api.list(query)

    override suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao =
        api.add(instituicao)

    override suspend fun updateInstituicaoApi(id: Int, instituicao: Instituicao): Instituicao =
        api.update(id, instituicao)
}
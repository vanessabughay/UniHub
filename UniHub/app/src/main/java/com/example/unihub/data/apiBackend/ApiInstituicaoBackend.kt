package com.example.unihub.data.apiBackend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.model.Instituicao
import com.example.unihub.BuildConfig
import com.example.unihub.data.api.InstituicaoApi
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository._instituicaobackend
import okhttp3.OkHttpClient

class ApiInstituicaoBackend : _instituicaobackend {
    private val api: InstituicaoApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                TokenManager.token?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InstituicaoApi::class.java)
    }

    override suspend fun buscarInstituicoesApi(query: String): List<Instituicao> =
        api.list(query)

    override suspend fun addInstituicaoApi(instituicao: Instituicao): Instituicao =
        api.add(instituicao)

    override suspend fun updateInstituicaoApi(id: Long, instituicao: Instituicao): Instituicao =
        api.update(id, instituicao)
}
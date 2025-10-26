package com.example.unihub.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.2:8080/"
   

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val disciplinaApiService: DisciplinaApiService by lazy {
        retrofit.create(DisciplinaApiService::class.java)
    }

    val compartilhamentoApiService: CompartilhamentoApiService by lazy {
        retrofit.create(CompartilhamentoApiService::class.java)
    }
}

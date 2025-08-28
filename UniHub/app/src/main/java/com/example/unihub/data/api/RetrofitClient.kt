package com.example.unihub.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.unihub.data.api.UniHubApi // Importa a sua interface de API
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import java.time.LocalDate

object RetrofitClient {
    // URL base do seu backend Spring Boot. Use o endereço do seu emulador.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // GsonBuilder para lidar com a conversão de LocalDate.
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Instância da sua API para ser usada no repositório.
    val api: UniHubApi by lazy {
        retrofit.create(UniHubApi::class.java)
    }
}
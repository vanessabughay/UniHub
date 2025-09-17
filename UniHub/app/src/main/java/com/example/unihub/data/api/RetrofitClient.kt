package com.example.unihub.data.api


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // ajuda a ver se o Authorization está indo
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthHeaderInterceptor())
        .addInterceptor(logging)
        .build()


    val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Instância da sua API para ser usada no repositório.
    val api: UniHubApi by lazy {
        retrofit.create(UniHubApi::class.java)
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
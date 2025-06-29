package com.example.unihub.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Para o emulador Android acessar o localhost da sua máquina, use 10.0.2.2
    // Se estiver testando em um dispositivo físico, ele precisará acessar o IP da sua máquina na rede local, ex: "http://192.168.1.XX:8080/"
    private const val BASE_URL = "http://10.0.2.2:8080/" // Onde seu Spring Boot está rodando

    // Por enquanto, vamos manter uma instância da DisciplinaApiService.
    // Em um projeto maior, você pode ter várias interfaces de serviço.
    val disciplinaService: DisciplinaApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(DisciplinaApiService::class.java)
    }
}
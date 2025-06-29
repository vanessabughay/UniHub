package com.example.unihub.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // IMPORT NECESSÁRIO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // IMPORT NECESSÁRIO PARA TimeUnit

object RetrofitClient {
    // Para o emulador Android acessar o localhost da sua máquina, use 10.0.2.2
    // Se estiver testando em um dispositivo físico, ele precisará acessar o IP da sua máquina na rede local, ex: "http://192.168.1.XX:8080/"
    private const val BASE_URL = "http:/10.0.2.2:8080/" // Onde seu Spring Boot está rodando

    val disciplinaService: DisciplinaApiService by lazy {
        // 1. Cria um interceptor de log para ver as requisições e respostas no Logcat
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Define o nível de log:
            // NONE: Sem logs.
            // BASIC: Loga URL, método, código de resposta e tamanho da resposta.
            // HEADERS: Loga todas as informações do BASIC + cabeçalhos.
            // BODY: Loga todas as informações do HEADERS + corpo (body) da requisição e resposta.
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        // 2. Cria um OkHttpClient personalizado
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Adiciona o interceptor de log
            // Configura timeouts para conexão, leitura e escrita
            .connectTimeout(30, TimeUnit.SECONDS) // Tempo máximo para estabelecer a conexão
            .readTimeout(30, TimeUnit.SECONDS)    // Tempo máximo entre pacotes de dados
            .writeTimeout(30, TimeUnit.SECONDS)   // Tempo máximo para enviar dados
            .build()

        // 3. Constrói o Retrofit usando o OkHttpClient personalizado
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient) // Usa o cliente OkHttp configurado
            .addConverterFactory(GsonConverterFactory.create()) // Converter JSON para objetos Kotlin
            .build()

        retrofit.create(DisciplinaApiService::class.java)
    }
}
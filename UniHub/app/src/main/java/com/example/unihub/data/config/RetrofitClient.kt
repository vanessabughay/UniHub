package com.example.unihub.data.config



/////// TESTE para utilizar no CELULAR
/// INCLJUÍDOS linhas 9-11    30-33   54-56
///Comentadas linhas 28  52

//////////////  INCLUIDOS   ///////////////////////////////////
import com.example.unihub.BuildConfig
///////////////////////////////////////////////////////////////

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.unihub.data.api.UniHubApi // Importa a sua interface de API
import com.example.unihub.data.util.LocalDateAdapter
import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.util.concurrent.TimeUnit




object RetrofitClient {
    // URL base do seu backend Spring Boot. Use o endereço do seu emulador.
    //private const val BASE_URL = "http://10.0.2.2:8080/"

    //////////////  INCLUIDOS   ///////////////////////////////////
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/"
    private val resolvedBaseUrl: String = BuildConfig.BASE_URL.ifBlank { DEFAULT_BASE_URL }
    ///////////////////////////////////////////////////////////////


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
        //.baseUrl(BASE_URL)

        //////////////  INCLUIDOS   ///////////////////////////////////
        .baseUrl(resolvedBaseUrl)
        ///////////////////////////////////////////////////////////////


        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Instância da sua API para ser usada no repositório.
    val api: UniHubApi by lazy {
        retrofit.create(UniHubApi::class.java)
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)

    //TESTE DO JSON

    // Função ou módulo que fornece a instância do OkHttpClient
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Defina o nível de log.
            // Level.BODY mostra cabeçalhos e corpo da requisição/resposta.
            // Level.HEADERS mostra apenas cabeçalhos.
            // Level.BASIC mostra a linha de requisição/resposta e o tempo.
            // Level.NONE não loga nada (para produção).
            level = HttpLoggingInterceptor.Level.BODY // <<< IMPORTANTE PARA VER O JSON
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Adiciona o interceptor de logging
            .connectTimeout(30, TimeUnit.SECONDS) // Exemplo de outras configurações
            .readTimeout(30, TimeUnit.SECONDS)    // Exemplo de outras configurações
            .writeTimeout(30, TimeUnit.SECONDS)   // Exemplo de outras configurações
            // .addInterceptor(OutroInterceptorSeVoceTiver()) // Você pode adicionar outros interceptors
            .build()
    }
    // Função ou módulo que fornece a instância do Retrofit
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient) // <<< PASSA O OkHttpClient CONFIGURADO
            .addConverterFactory(GsonConverterFactory.create()) // Ou seu conversor
            .build()
    }




}
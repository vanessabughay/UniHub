package com.example.unihub.data.api

import com.example.unihub.data.api.model.AuthResponse
import com.example.unihub.data.api.model.LoginRequest
import com.example.unihub.data.api.model.RegisterRequest
import com.example.unihub.data.api.model.UpdateUsuarioRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface UniHubApi {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Void>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @PUT("usuarios/me")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Body request: UpdateUsuarioRequest
    ): Response<Void>
}
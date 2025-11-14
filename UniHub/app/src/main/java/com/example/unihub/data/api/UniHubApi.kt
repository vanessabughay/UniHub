package com.example.unihub.data.api

import com.example.unihub.data.api.model.AuthResponse
import com.example.unihub.data.api.model.LoginRequest
import com.example.unihub.data.api.model.RegisterRequest
import com.example.unihub.data.api.model.UpdateUsuarioRequest
import com.example.unihub.data.api.model.SolicitarRedefinicaoSenhaRequest
import com.example.unihub.data.api.model.RedefinirSenhaRequest
import com.example.unihub.data.api.model.GoogleLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT

interface UniHubApi {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Void>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @PUT("usuarios/me")
    suspend fun updateUser(
        @Body request: UpdateUsuarioRequest
    ): Response<Void>

    @DELETE("usuarios/me")
    suspend fun deleteUser(): Response<Void>

    @POST("api/auth/forgot-password")
    suspend fun solicitarRedefinicaoSenha(
        @Body body: SolicitarRedefinicaoSenhaRequest
    ): Response<Void>

    @POST("/api/auth/reset-password")
    suspend fun redefinirSenha(@Body body: RedefinirSenhaRequest): Response<Void>

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<AuthResponse>

}
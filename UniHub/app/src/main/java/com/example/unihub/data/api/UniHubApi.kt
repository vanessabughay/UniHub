package com.example.unihub.data.api

import com.example.unihub.data.api.model.AuthResponse
import com.example.unihub.data.api.model.LoginRequest
import com.example.unihub.data.api.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UniHubApi {
    @POST("api/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<Void>

    @POST("api/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>
}
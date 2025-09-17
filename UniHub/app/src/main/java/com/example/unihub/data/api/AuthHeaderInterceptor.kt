package com.example.unihub.data.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val t = TokenManager.token
        val req = if (t.isNullOrBlank()) chain.request()
        else chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $t")
            .build()
        return chain.proceed(req)
    }
}

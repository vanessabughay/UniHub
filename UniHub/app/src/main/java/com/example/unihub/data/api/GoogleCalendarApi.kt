package com.example.unihub.data.api

import com.example.unihub.data.api.model.GoogleCalendarLinkRequest
import com.example.unihub.data.api.model.GoogleCalendarStatusResponse
import com.example.unihub.data.api.model.GoogleCalendarSyncResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface GoogleCalendarApi {
    @GET("api/google/calendar/status")
    suspend fun status(): Response<GoogleCalendarStatusResponse>

    @POST("api/google/calendar/link")
    suspend fun link(@Body body: GoogleCalendarLinkRequest): Response<GoogleCalendarStatusResponse>

    @DELETE("api/google/calendar/link")
    suspend fun unlink(): Response<GoogleCalendarStatusResponse>

    @POST("api/google/calendar/sync")
    suspend fun sync(): Response<GoogleCalendarSyncResponse>
}
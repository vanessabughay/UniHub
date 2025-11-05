package com.example.unihub.data.repository

import android.content.Context
import com.example.unihub.data.api.GoogleCalendarApi
import com.example.unihub.data.api.model.GoogleCalendarLinkRequest
import com.example.unihub.data.api.model.GoogleCalendarStatusResponse
import com.example.unihub.data.api.model.GoogleCalendarSyncResponse
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.config.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeParseException

class GoogleCalendarRepository(
    private val api: GoogleCalendarApi = RetrofitClient.create(GoogleCalendarApi::class.java)
) {

    suspend fun fetchStatus(context: Context): GoogleCalendarStatus = withContext(Dispatchers.IO) {
        try {
            val response = api.status()
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException("Resposta vazia do servidor")
                val status = body.toDomain()
                TokenManager.updateCalendarLinkState(context, status.linked)
                status
            } else {
                throw IOException("Erro ao carregar status do Google Calendar: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            throw IOException("Erro de servidor ao consultar o status do Google Calendar: ${e.code()}", e)
        }
    }

    suspend fun link(context: Context, authCode: String): GoogleCalendarStatus = withContext(Dispatchers.IO) {
        try {
            val response = api.link(GoogleCalendarLinkRequest(authCode))
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException("Resposta vazia ao vincular Google Calendar")
                val status = body.toDomain()
                TokenManager.updateCalendarLinkState(context, status.linked)
                status
            } else {
                throw IOException("Erro ao vincular Google Calendar: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            throw IOException("Erro de servidor ao vincular Google Calendar: ${e.code()}", e)
        }
    }

    suspend fun unlink(context: Context): GoogleCalendarStatus = withContext(Dispatchers.IO) {
        try {
            val response = api.unlink()
            if (response.isSuccessful) {
                val body = response.body() ?: GoogleCalendarStatusResponse(false, null, false)
                val status = body.toDomain()
                TokenManager.updateCalendarLinkState(context, status.linked)
                status
            } else {
                throw IOException("Erro ao desvincular Google Calendar: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            throw IOException("Erro de servidor ao desvincular Google Calendar: ${e.code()}", e)
        }
    }

    suspend fun sync(context: Context): GoogleCalendarSyncResult = withContext(Dispatchers.IO) {
        try {
            val response = api.sync()
            if (response.isSuccessful) {
                val body = response.body() ?: throw IOException("Resposta vazia ao sincronizar Google Calendar")
                val result = body.toDomain()
                TokenManager.updateCalendarLinkState(context, true)
                result
            } else {
                throw IOException("Erro ao sincronizar com Google Calendar: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: HttpException) {
            throw IOException("Erro de servidor ao sincronizar Google Calendar: ${e.code()}", e)
        }
    }

    private fun GoogleCalendarStatusResponse.toDomain(): GoogleCalendarStatus = GoogleCalendarStatus(
        linked = linked,
        lastSyncedAt = parseInstant(lastSyncedAt),
        requiresReauth = requiresReauth
    )

    private fun GoogleCalendarSyncResponse.toDomain(): GoogleCalendarSyncResult = GoogleCalendarSyncResult(
        synced = synced,
        failures = failures,
        lastSyncedAt = parseInstant(lastSyncedAt)
    )

    private fun parseInstant(value: String?): Instant? = try {
        value?.let { Instant.parse(it) }
    } catch (e: DateTimeParseException) {
        null
    }
}

data class GoogleCalendarStatus(
    val linked: Boolean,
    val lastSyncedAt: Instant?,
    val requiresReauth: Boolean
)

data class GoogleCalendarSyncResult(
    val synced: Int,
    val failures: Int,
    val lastSyncedAt: Instant?
)
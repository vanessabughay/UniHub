package com.example.unihub.data.repository

import com.example.unihub.data.api.NotificationLogApi
import com.example.unihub.data.config.RetrofitClient
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.NotificationLogRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationRemoteLogger {

    private val api: NotificationLogApi by lazy { RetrofitClient.create(NotificationLogApi::class.java) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun logNotification(
        title: String?,
        message: String,
        timestampMillis: Long,
        type: String?,
        category: String?,
        referenceId: Long?,
        hasPendingInteraction: Boolean,
        metadata: Map<String, Any?>?
    ) {
        scope.launch {
            try {
                if (TokenManager.ensureTokenLoaded().isNullOrBlank()) return@launch
                val request = NotificationLogRequest(
                    titulo = title,
                    mensagem = message,
                    tipo = type,
                    categoria = category,
                    referenciaId = referenceId,
                    interacaoPendente = hasPendingInteraction,
                    metadata = metadata,
                    timestamp = timestampMillis
                )
                api.logNotification(request)
            } catch (_: Exception) {
                // Ignora falhas de sincronização para não impactar o fluxo local
            }
        }
    }
}
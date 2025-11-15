package com.example.unihub.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.CompartilhamentoBackend
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.config.TokenManager
import com.example.unihub.ui.ListarDisciplinas.NotificacaoConviteUi

class CompartilhamentoNotificationSynchronizer private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = CompartilhamentoNotificationManager(appContext)
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)
    private val repository = CompartilhamentoRepository(createBackend())
    private val preferences: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun refreshFromBackend(usuarioId: Long) {
        val notifications = repository.listarNotificacoes(usuarioId).map { it.toUi() }
        synchronize(notifications)
    }

    fun synchronize(notifications: List<NotificacaoConviteUi>) {
        val validReferences = notifications
            .mapNotNull { it.historyReferenceId() }
            .toSet()
        historyRepository.pruneShareNotifications(validReferences)
        // 1) Marca como tratados todos os convites já lidos
        notifications
            .filter { it.isInviteAlreadyHandled() }
            .mapNotNull { it.conviteId }
            .forEach { inviteId ->
                historyRepository.markShareInviteHandled(inviteId)
            }

        // 2) Mantém apenas os ainda não tratados
        val filteredNotifications = notifications.filterNot { it.isInviteAlreadyHandled() }

        // 3) Calcula convites pendentes atuais (somente os válidos)
        val pendingInvites = filteredNotifications.filter { it.isPendingInvite() }
        val currentInviteIds = pendingInvites.mapNotNull { it.conviteId }.toSet()

        // 4) Descobre convites que sumiram (foram respondidos/cancelados no backend)
        val previousInviteIds = loadActiveInviteIds()
        val removedInvites = previousInviteIds - currentInviteIds
        removedInvites.forEach { inviteId ->
            historyRepository.markShareInviteHandled(inviteId)
            removeInvite(inviteId)
        }

        // 5) Mostra convites com UI especial
        pendingInvites.forEach { invite ->
            notificationManager.showInviteNotification(invite)
        }

        // 6) Mostra o restante de forma genérica e registra no histórico
        val otherNotifications = filteredNotifications.filterNot { it.isPendingInvite() }
        otherNotifications.forEach { notif ->
            notificationManager.showGenericNotification(notif)
        }

        // 7) Persiste os convites ativos do momento
        saveActiveInviteIds(currentInviteIds)
    }

    fun completeInvite(inviteId: Long) {
        historyRepository.markShareInviteHandled(inviteId)
        removeInvite(inviteId)
    }

    fun silenceNotifications() {
        saveActiveInviteIds(emptySet())
        notificationManager.cancelAll()
        historyRepository.clearByCategoryOrType(
            category = NotificationHistoryRepository.SHARE_CATEGORY,
            types = setOf(
                NotificationHistoryRepository.SHARE_INVITE_TYPE,
                NotificationHistoryRepository.SHARE_INVITE_TYPE_RESPONSE,
            )
        )
    }

    fun reset() {
        historyRepository.clear()
        notificationManager.cancelAll()
        preferences.edit().remove(KEY_ACTIVE_INVITES).apply()
    }

    private fun removeInvite(inviteId: Long) {
        notificationManager.cancelNotification(notificationManager.notificationIdForInvite(inviteId))
        val updated = loadActiveInviteIds() - inviteId
        saveActiveInviteIds(updated)
    }

    private fun loadActiveInviteIds(): Set<Long> {
        val stored = preferences.getStringSet(KEY_ACTIVE_INVITES, emptySet()) ?: emptySet()
        return stored.mapNotNull { it.toLongOrNull() }.toSet()
    }

    private fun saveActiveInviteIds(ids: Set<Long>) {
        preferences.edit()
            .putStringSet(KEY_ACTIVE_INVITES, ids.map { it.toString() }.toSet())
            .apply()
    }

    private fun NotificacaoConviteUi.historyReferenceId(): Long? {
        return conviteId ?: referenciaId ?: id
    }
    private fun createBackend(): CompartilhamentoBackend = ApiCompartilhamentoBackend()

    private fun NotificacaoResponse.toUi(): NotificacaoConviteUi = NotificacaoConviteUi(
        id = id,
        titulo = titulo,
        mensagem = mensagem,
        conviteId = conviteId,
        tipo = tipo,
        lida = lida,
        criadaEm = criadaEm,
        categoria = categoria,
        referenciaId = referenciaId,
        interacaoPendente = interacaoPendente,
        metadataJson = metadataJson,
        atualizadaEm = atualizadaEm
    )

    private fun NotificacaoConviteUi.isPendingInvite(): Boolean {
        return tipo == TIPO_CONVITE && conviteId != null && (interacaoPendente || !lida)
    }

    private fun NotificacaoConviteUi.isInviteAlreadyHandled(): Boolean {
        return tipo == TIPO_CONVITE && conviteId != null && !interacaoPendente && lida
    }


    companion object {
        private const val PREFS_NAME = "share_notification_synchronizer"
        private const val KEY_ACTIVE_INVITES = "active_invites"
        private const val TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val isRunning = AtomicBoolean(false)

        fun getInstance(context: Context): CompartilhamentoNotificationSynchronizer {
            return CompartilhamentoNotificationSynchronizer(context)
        }

        fun triggerImmediate(context: Context) {
            val appContext = context.applicationContext
            val userId = TokenManager.usuarioId ?: return
            if (!isRunning.compareAndSet(false, true)) {
                return
            }
            scope.launch {
                try {
                    getInstance(appContext).refreshFromBackend(userId)
                } catch (_: Exception) {
                    // Ignore refresh errors silently; user can retry later.
                } finally {
                    isRunning.set(false)
                }
            }
        }

        fun broadcastRefresh(context: Context) {
            val intent = Intent(CompartilhamentoNotificationActionReceiver.ACTION_REFRESH).apply {
                `package` = context.packageName
            }
            context.sendBroadcast(intent)
        }
    }
}
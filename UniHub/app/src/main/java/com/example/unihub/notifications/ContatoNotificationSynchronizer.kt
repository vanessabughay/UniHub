package com.example.unihub.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.model.NotificacaoResponse
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.ui.ListarDisciplinas.NotificacaoConviteUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ContatoNotificationSynchronizer private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val preferences: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)
    private val notificationManager = ContatoNotificationManager(appContext)
    private val repository = CompartilhamentoRepository(ApiCompartilhamentoBackend())

    suspend fun refreshFromBackend(userId: Long) {
        val responses = repository.listarNotificacoes(userId)
        val notifications = responses.map { it.toUi() }
        synchronize(notifications)
    }

    fun synchronize(notifications: List<NotificacaoConviteUi>) {
        val contactNotifications = notifications.filter { it.isContactNotification() }

        val validReferences = contactNotifications
            .mapNotNull { it.historyReferenceId() }
            .toSet()
        historyRepository.pruneContactNotifications(validReferences)

        val handledInviteIds = contactNotifications
            .filter { it.isInviteAlreadyHandled() }
            .mapNotNull { it.referenciaId }
            .toSet()

        handledInviteIds.forEach { inviteId ->
            historyRepository.markContactInviteHandled(inviteId)
            removeInvite(inviteId)
        }

        val pendingInvites = contactNotifications.filter { it.isPendingInvite() }
        val currentInviteIds = pendingInvites.mapNotNull { it.referenciaId }.toSet()

        val storedInvites = loadActiveInviteIds()
        val removedInvites = storedInvites - currentInviteIds
        removedInvites.forEach { inviteId ->
            historyRepository.markContactInviteHandled(inviteId)
            removeInvite(inviteId)
        }

        pendingInvites.forEach { invite ->
            notificationManager.showInviteNotification(invite)
        }

        val otherNotifications = contactNotifications
            .filterNot { it.isPendingInvite() }
            .filter { it.tipo?.equals(TIPO_RESPOSTA, ignoreCase = true) == true }
        otherNotifications.forEach { notification ->
            notificationManager.showGenericNotification(notification)
        }

        saveActiveInviteIds(currentInviteIds)
    }

    fun completeInvite(inviteId: Long) {
        historyRepository.markContactInviteHandled(inviteId)
        removeInvite(inviteId)
    }

    fun silenceNotifications() {
        saveActiveInviteIds(emptySet())
        notificationManager.cancelAll()
        historyRepository.clearByCategoryOrType(
            category = NotificationHistoryRepository.CONTACT_CATEGORY,
            types = setOf(
                TIPO_CONVITE,
                TIPO_RESPOSTA,
                NotificationHistoryRepository.CONTACT_INVITE_TYPE,
                NotificationHistoryRepository.CONTACT_INVITE_TYPE_RESPONSE,
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
        return referenciaId ?: conviteId ?: id
    }

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

    private fun NotificacaoConviteUi.isContactNotification(): Boolean {
        val normalizedType = tipo?.trim()
        if (!normalizedType.isNullOrBlank() && normalizedType.contains(CONTACT_KEYWORD, ignoreCase = true)) {
            return true
        }
        val normalizedCategory = categoria?.trim()
        if (!normalizedCategory.isNullOrBlank() && normalizedCategory.contains(CONTACT_KEYWORD, ignoreCase = true)) {
            return true
        }
        return false
    }

    private fun NotificacaoConviteUi.isPendingInvite(): Boolean {
        val referenceId = referenciaId ?: return false
        return tipo.equals(TIPO_CONVITE, ignoreCase = true) && (interacaoPendente || !lida) && referenceId >= 0
    }

    private fun NotificacaoConviteUi.isInviteAlreadyHandled(): Boolean {
        val referenceId = referenciaId ?: return false
        return tipo == TIPO_CONVITE && !interacaoPendente && lida && referenceId >= 0
    }

    companion object {
        private const val PREFS_NAME = "contact_notification_synchronizer"
        private const val KEY_ACTIVE_INVITES = "contact_active_invites"
        private const val TIPO_CONVITE = "CONTATO_SOLICITACAO"
        private const val TIPO_RESPOSTA = "CONTATO_SOLICITACAO_RESPOSTA"
        private const val CONTACT_CATEGORY = "CONTATO"
        private const val CONTACT_KEYWORD = "CONTATO"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val isRunning = AtomicBoolean(false)

        fun getInstance(context: Context): ContatoNotificationSynchronizer {
            return ContatoNotificationSynchronizer(context)
        }

        fun triggerImmediate(context: Context) {
            val appContext = context.applicationContext
            TokenManager.loadToken(appContext)
            val userId = TokenManager.usuarioId ?: return
            if (!isRunning.compareAndSet(false, true)) {
                return
            }
            scope.launch {
                try {
                    getInstance(appContext).refreshFromBackend(userId)
                } catch (_: Exception) {
                    // Ignora erros de sincronização
                } finally {
                    isRunning.set(false)
                }
            }
        }

        fun broadcastRefresh(context: Context) {
            val intent = Intent(ContatoNotificationActionReceiver.ACTION_REFRESH).apply {
                `package` = context.packageName
            }
            context.sendBroadcast(intent)
        }
    }
}
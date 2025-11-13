package com.example.unihub.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.unihub.R
import com.example.unihub.data.apiBackend.ApiContatoBackend
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.ContatoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContatoNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val referenceId = intent.getLongExtra(EXTRA_REFERENCE_ID, -1L)
        val action = intent.action

        if (notificationId == -1 || referenceId == -1L || action.isNullOrBlank()) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                TokenManager.loadToken(appContext)
                val repository = ContatoRepository(ApiContatoBackend())

                when (action) {
                    ACTION_ACCEPT -> repository.acceptInvitation(referenceId)
                    ACTION_REJECT -> repository.rejectInvitation(referenceId)
                }

                val historyRepository = NotificationHistoryRepository.getInstance(appContext)
                val messageRes = when (action) {
                    ACTION_ACCEPT -> R.string.contact_notification_history_accept
                    ACTION_REJECT -> R.string.contact_notification_history_reject
                    else -> null
                }

                val title = historyRepository.historyFlow.value.firstOrNull { entry ->
                    entry.referenceId == referenceId &&
                            entry.category?.equals(CONTACT_CATEGORY, ignoreCase = true) == true
                }?.title ?: appContext.getString(R.string.contact_notification_history_title)

                messageRes?.let { resId ->
                    historyRepository.updateContactNotification(
                        referenceId = referenceId,
                        title = title,
                        message = appContext.getString(resId),
                        timestampMillis = System.currentTimeMillis(),
                        type = ContatoNotificationManager.TIPO_RESPOSTA,
                    )
                }

                ContatoNotificationSynchronizer.getInstance(appContext).completeInvite(referenceId)
                notifyRefresh(appContext)
                ContatoNotificationSynchronizer.triggerImmediate(appContext)
            } catch (_: Exception) {
                // Mantém a notificação para tentar novamente mais tarde
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyRefresh(context: Context) {
        val intent = Intent(ACTION_REFRESH).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(intent)
    }

    companion object {
        const val ACTION_ACCEPT = "com.example.unihub.action.ACCEPT_CONTACT_INVITE"
        const val ACTION_REJECT = "com.example.unihub.action.REJECT_CONTACT_INVITE"
        const val ACTION_REFRESH = "com.example.unihub.action.REFRESH_CONTACT_NOTIFICATIONS"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_REFERENCE_ID = "extra_reference_id"
        private const val CONTACT_CATEGORY = "CONTATO"
    }
}
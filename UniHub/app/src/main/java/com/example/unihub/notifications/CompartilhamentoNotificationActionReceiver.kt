package com.example.unihub.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.unihub.data.apiBackend.ApiCompartilhamentoBackend
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.CompartilhamentoBackend
import com.example.unihub.data.repository.CompartilhamentoRepository
import com.example.unihub.data.repository.NotificationHistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompartilhamentoNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val conviteId = intent.getLongExtra(EXTRA_CONVITE_ID, -1)
        val action = intent.action

        if (notificationId == -1 || conviteId == -1L || action == null) {
            return
        }

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TokenManager.loadToken(appContext)
                val usuarioId = TokenManager.usuarioId
                if (usuarioId != null) {
                    val repository = CompartilhamentoRepository(createBackend())
                    when (action) {
                        ACTION_ACCEPT -> repository.aceitarConvite(conviteId, usuarioId)
                        ACTION_REJECT -> repository.rejeitarConvite(conviteId, usuarioId)
                    }

                    NotificationHistoryRepository.getInstance(appContext).updateShareInviteResponse(
                        referenceId = conviteId,
                        accepted = action == ACTION_ACCEPT,
                        timestampMillis = System.currentTimeMillis()
                    )

                    CompartilhamentoNotificationSynchronizer.getInstance(appContext)
                        .completeInvite(conviteId)

                    notifyRefresh(appContext)
                    CompartilhamentoNotificationSynchronizer.triggerImmediate(appContext)
                }
            } catch (_: Exception) {
                // In case of error we keep the notification so the user can retry.
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyRefresh(context: Context) {
        val broadcastIntent = Intent(ACTION_REFRESH).apply {
            `package` = context.packageName
        }
        context.sendBroadcast(broadcastIntent)
    }

    private fun createBackend(): CompartilhamentoBackend = ApiCompartilhamentoBackend()

    companion object {
        const val ACTION_ACCEPT = "com.example.unihub.action.ACCEPT_SHARE_INVITE"
        const val ACTION_REJECT = "com.example.unihub.action.REJECT_SHARE_INVITE"
        const val ACTION_REFRESH = "com.example.unihub.action.REFRESH_SHARE_NOTIFICATIONS"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_CONVITE_ID = "extra_convite_id"
        private const val CATEGORY_COMPARTILHAMENTO = "COMPARTILHAMENTO"
    }
}
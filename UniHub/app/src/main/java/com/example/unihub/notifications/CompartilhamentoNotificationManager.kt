package com.example.unihub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.unihub.MainActivity
import com.example.unihub.R
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.ui.ListarDisciplinas.NotificacaoConviteUi

/**
 * Handles showing discipline sharing notifications and logging them in the
 * local history so that they appear in the notification timeline screen.
 */
class CompartilhamentoNotificationManager(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(appContext)
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)

    fun showNotification(notification: NotificacaoConviteUi) {
        ensureChannels()
        val notificationId = notificationId(notification)
        val (channelId, titleRes, autoCancel) = when (notification.tipo) {
            TIPO_CONVITE -> Triple(
                CHANNEL_INVITES,
                R.string.share_notification_invite_title,
                false
            )
            TIPO_RESPOSTA -> Triple(
                CHANNEL_RESPONSES,
                R.string.share_notification_response_title,
                true
            )
            else -> Triple(
                CHANNEL_RESPONSES,
                R.string.share_notification_generic_title,
                true
            )
        }

        val title = appContext.getString(titleRes)
        val message = notification.mensagem
        val isInvite = isInvite(notification)

        val contentIntent = buildContentIntent(notificationId)

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(autoCancel)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOngoing(isInvite)

        if (isInvite && notification.conviteId != null) {
            val acceptIntent = createActionIntent(
                action = CompartilhamentoNotificationActionReceiver.ACTION_ACCEPT,
                notification = notification,
                notificationId = notificationId
            )
            val rejectIntent = createActionIntent(
                action = CompartilhamentoNotificationActionReceiver.ACTION_REJECT,
                notification = notification,
                notificationId = notificationId
            )

            builder.addAction(
                0,
                appContext.getString(R.string.share_notification_action_accept),
                acceptIntent
            )
            builder.addAction(
                0,
                appContext.getString(R.string.share_notification_action_reject),
                rejectIntent
            )
        }

        notificationManager.notify(notificationId, builder.build())

        historyRepository.logNotification(
            title = title,
            message = message,
            timestampMillis = System.currentTimeMillis(),
            shareInviteId = notification.conviteId,
            shareActionPending = isInvite
        )
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun notificationId(notification: NotificacaoConviteUi): Int {
        val key = notification.conviteId ?: notification.id
        return (key xor (key shr 32)).toInt()
    }

    fun notificationIdForInvite(inviteId: Long): Int {
        return (inviteId xor (inviteId shr 32)).toInt()
    }

    private fun isInvite(notification: NotificacaoConviteUi): Boolean {
        return notification.tipo == TIPO_CONVITE && notification.conviteId != null
    }

    private fun createActionIntent(
        action: String,
        notification: NotificacaoConviteUi,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(appContext, CompartilhamentoNotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(CompartilhamentoNotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(CompartilhamentoNotificationActionReceiver.EXTRA_CONVITE_ID, notification.conviteId)
        }

        val requestCode = when (action) {
            CompartilhamentoNotificationActionReceiver.ACTION_ACCEPT -> notificationId shl 1
            CompartilhamentoNotificationActionReceiver.ACTION_REJECT -> (notificationId shl 1) + 1
            else -> notificationId
        }

        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or AttendanceNotificationScheduler.immutableFlag()
        )
    }

    private fun buildContentIntent(notificationId: Int): PendingIntent {
        val openIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return TaskStackBuilder.create(appContext).run {
            addNextIntentWithParentStack(openIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or AttendanceNotificationScheduler.immutableFlag()
            )
        }
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return

        val inviteChannel = NotificationChannel(
            CHANNEL_INVITES,
            appContext.getString(R.string.share_notification_invite_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.share_notification_invite_channel_description)
        }

        val responseChannel = NotificationChannel(
            CHANNEL_RESPONSES,
            appContext.getString(R.string.share_notification_response_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(R.string.share_notification_response_channel_description)
        }

        manager.createNotificationChannel(inviteChannel)
        manager.createNotificationChannel(responseChannel)
    }

    private companion object {
        private const val CHANNEL_INVITES = "compartilhamento_invites"
        private const val CHANNEL_RESPONSES = "compartilhamento_responses"
        private const val TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO"
        const val TIPO_RESPOSTA = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA"
    }
}
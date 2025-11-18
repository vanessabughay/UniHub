package com.example.unihub.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.example.unihub.MainActivity
import com.example.unihub.R
import com.example.unihub.data.repository.NotificationHistoryRepository
import com.example.unihub.ui.ListarDisciplinas.NotificacaoConviteUi
import java.time.LocalDateTime
import java.time.ZoneId

class ContatoNotificationManager(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(appContext)
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)
    private val notificationZone: ZoneId = ZoneId.of("America/Sao_Paulo")

    private fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return notificationManager.areNotificationsEnabled()
    }

    private fun safeNotify(notificationId: Int, builder: NotificationCompat.Builder) {
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Ignora se não houver permissão no momento
        }
    }

    fun showInviteNotification(invite: NotificacaoConviteUi) {
        val referenceId = invite.referenciaId ?: return
        if (invite.lida && !invite.interacaoPendente) return

        val notificationId = notificationIdForInvite(referenceId)
        val title = invite.titulo ?: appContext.getString(R.string.contact_notification_invite_title)
        val message = invite.mensagem

        historyRepository.logNotification(
            title = title,
            message = message,
            timestampMillis = invite.historyTimestampMillis(),
            type = invite.tipo,
            category = CONTACT_CATEGORY,
            referenceId = referenceId,
            hasPendingInteraction = true,
            syncWithBackend = false,
        )

        if (!hasPostNotificationsPermission()) return

        ensureChannels()

        val contentIntent = buildContentIntent(notificationId)

        val builder = NotificationCompat.Builder(appContext, CHANNEL_INVITES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOngoing(true)

        val acceptIntent = createActionIntent(
            action = ContatoNotificationActionReceiver.ACTION_ACCEPT,
            referenceId = referenceId,
            notificationId = notificationId
        )
        val rejectIntent = createActionIntent(
            action = ContatoNotificationActionReceiver.ACTION_REJECT,
            referenceId = referenceId,
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

        safeNotify(notificationId, builder)
    }

    fun showGenericNotification(notification: NotificacaoConviteUi) {
        val notificationId = notificationId(notification)
        val (channelId, titleRes) = when (notification.tipo) {
            TIPO_RESPOSTA -> CHANNEL_RESPONSES to R.string.contact_notification_response_title
            else -> CHANNEL_RESPONSES to R.string.contact_notification_generic_title
        }

        val title = notification.titulo ?: appContext.getString(titleRes)
        val message = notification.mensagem
        val referenceId = notification.referenciaId ?: notification.id

        val timestamp = notification.historyTimestampMillis()
        val shouldNotify = if (notification.tipo == TIPO_RESPOSTA) {
            historyRepository.updateContactNotification(
                referenceId = referenceId,
                title = title,
                message = message,
                timestampMillis = timestamp,
                type = TIPO_RESPOSTA
            )
        } else {
            historyRepository.logNotification(
                title = title,
                message = message,
                timestampMillis = timestamp,
                type = notification.tipo,
                category = CONTACT_CATEGORY,
                referenceId = referenceId,
                hasPendingInteraction = false,
                syncWithBackend = false,
            )
        }

        if (!shouldNotify) {
            return
        }

        if (notification.tipo == TIPO_CONVITE && notification.lida) return
        if (!hasPostNotificationsPermission()) return

        ensureChannels()

        val contentIntent = buildContentIntent(notificationId)

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOngoing(false)

        safeNotify(notificationId, builder)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }

    fun notificationId(notification: NotificacaoConviteUi): Int {
        val key: Long = notification.referenciaId ?: notification.id
        return (key xor (key ushr 32)).toInt()
    }

    fun notificationIdForInvite(referenceId: Long): Int {
        return (referenceId xor (referenceId ushr 32)).toInt()
    }

    private fun createActionIntent(
        action: String,
        referenceId: Long,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(appContext, ContatoNotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(ContatoNotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(ContatoNotificationActionReceiver.EXTRA_REFERENCE_ID, referenceId)
        }

        val requestCode = when (action) {
            ContatoNotificationActionReceiver.ACTION_ACCEPT -> notificationId shl 1
            ContatoNotificationActionReceiver.ACTION_REJECT -> (notificationId shl 1) + 1
            else -> notificationId
        }

        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or FrequenciaNotificationScheduler.immutableFlag()
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
                PendingIntent.FLAG_UPDATE_CURRENT or FrequenciaNotificationScheduler.immutableFlag()
            )
        }
    }

    private fun NotificacaoConviteUi.historyTimestampMillis(): Long {
        val isoString = atualizadaEm ?: criadaEm
        if (isoString.isNullOrBlank()) {
            return System.currentTimeMillis()
        }

        return runCatching {
            LocalDateTime.parse(isoString)
                .atZone(notificationZone)
                .toInstant()
                .toEpochMilli()
        }.getOrElse { System.currentTimeMillis() }
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return

        val inviteChannel = NotificationChannel(
            CHANNEL_INVITES,
            appContext.getString(R.string.contact_notification_invite_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = appContext.getString(R.string.contact_notification_invite_channel_description)
        }

        val responseChannel = NotificationChannel(
            CHANNEL_RESPONSES,
            appContext.getString(R.string.contact_notification_response_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(R.string.contact_notification_response_channel_description)
        }

        manager.createNotificationChannel(inviteChannel)
        manager.createNotificationChannel(responseChannel)
    }

    companion object {
        private const val CHANNEL_INVITES = "contato_invites"
        private const val CHANNEL_RESPONSES = "contato_responses"
        private const val TIPO_CONVITE = "CONTATO_SOLICITACAO"
        const val TIPO_RESPOSTA = "CONTATO_SOLICITACAO_RESPOSTA"
        private const val CONTACT_CATEGORY = "CONTATO"
    }
}
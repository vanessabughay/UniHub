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

class CompartilhamentoNotificationManager(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager = NotificationManagerCompat.from(appContext)
    private val historyRepository = NotificationHistoryRepository.getInstance(appContext)
    private val notificationZone: ZoneId = ZoneId.of("America/Sao_Paulo")

    // =====================================================
    // Permissão / capacidade de notificar
    // =====================================================
    private fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        // Também precisa estar habilitado a nível de sistema/canal
        return notificationManager.areNotificationsEnabled()
    }

    private fun safeNotify(notificationId: Int, builder: NotificationCompat.Builder) {
        try {
            notificationManager.notify(notificationId, builder.build())
        } catch (se: SecurityException) {
            // Aqui você pode logar/telemetria se quiser
            // Log.w("Notif", "Sem permissão para notificar", se)
        }
    }

    fun canNotify(): Boolean = hasPostNotificationsPermission()


    // ============ API pública especializada ============

    /**
     * Mostra convite com UI especial (canal de convite, ações aceitar/recusar, ongoing).
     * Ignora se convite for inválido (sem conviteId) ou já lido.
     */

    fun showInviteNotification(invite: NotificacaoConviteUi) {
        val inviteId = invite.conviteId ?: return
        if (invite.lida) return
        val notificationId = notificationIdForInvite(inviteId)
        val title = invite.titulo ?: appContext.getString(R.string.share_notification_invite_title)
        val message = invite.mensagem

        val historyReference = inviteId

        val (senderName, disciplineName) =
            NotificationHistoryRepository.guessShareInviteDetails(title, message)
        val metadata = mutableMapOf<String, Any?>()
        metadata[NotificationHistoryRepository.KEY_SHARE_INVITE_ID] = inviteId
        senderName?.let {
            metadata[NotificationHistoryRepository.KEY_SHARE_SENDER_NAME] = it
        }
        disciplineName?.let {
            metadata[NotificationHistoryRepository.KEY_SHARE_DISCIPLINE_NAME] = it
        }

        historyRepository.logNotification(
            title = title,
            message = message,
            timestampMillis = invite.historyTimestampMillis(),
            type = invite.tipo,
            category = SHARE_CATEGORY,
            referenceId = historyReference,
            hasPendingInteraction = true,
            metadata = metadata.takeIf { it.isNotEmpty() },
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
            .setAutoCancel(false) // convite fica até ação
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setOngoing(true)

        // Ações (Aceitar / Recusar)
        val acceptIntent = createActionIntent(
            action = CompartilhamentoNotificationActionReceiver.ACTION_ACCEPT,
            notification = invite,
            notificationId = notificationId
        )
        val rejectIntent = createActionIntent(
            action = CompartilhamentoNotificationActionReceiver.ACTION_REJECT,
            notification = invite,
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

    /**
     * Mostra qualquer notificação não-convite (ou convite não pendente) de forma genérica.
     */

    fun showGenericNotification(notification: NotificacaoConviteUi) {

        val notificationId = notificationId(notification)
        val (channelId, titleRes) = when (notification.tipo) {
            TIPO_RESPOSTA -> CHANNEL_RESPONSES to R.string.share_notification_response_title
            else -> CHANNEL_RESPONSES to R.string.share_notification_generic_title
        }

        val title = notification.titulo ?: appContext.getString(titleRes)
        val message = notification.mensagem
        val historyReference = notification.conviteId ?: notification.referenciaId ?: notification.id

        val metadata = mutableMapOf<String, Any?>()
        notification.conviteId?.let {
            metadata[NotificationHistoryRepository.KEY_SHARE_INVITE_ID] = it
        }

        val shouldNotify = historyRepository.logNotification(
            title = title,
            message = message,
            timestampMillis = notification.historyTimestampMillis(),
            type = notification.tipo,
            category = SHARE_CATEGORY,
            referenceId = historyReference,
            hasPendingInteraction = false,
            metadata = metadata.takeIf { it.isNotEmpty() },
            syncWithBackend = false,
        )
        if (!shouldNotify) {
            return
        }

        if (notification.lida) return


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

    // ============ Utilitários públicos ============

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAll() {
        notificationManager.cancelAll()
    }

    fun notificationId(notification: NotificacaoConviteUi): Int {
        val key: Long = notification.conviteId ?: notification.id
        return (key xor (key ushr 32)).toInt()
    }

    fun notificationIdForInvite(inviteId: Long): Int {
        return (inviteId xor (inviteId ushr 32)).toInt()
    }

    // ============ Internos ============

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

    companion object {
        private const val CHANNEL_INVITES = "compartilhamento_invites"
        private const val CHANNEL_RESPONSES = "compartilhamento_responses"
        private const val TIPO_CONVITE = "DISCIPLINA_COMPARTILHAMENTO"
        const val TIPO_RESPOSTA = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA"
        private const val SHARE_CATEGORY = "COMPARTILHAMENTO"
    }
}

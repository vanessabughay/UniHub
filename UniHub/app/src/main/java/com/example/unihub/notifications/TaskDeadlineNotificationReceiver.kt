package com.example.unihub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.unihub.MainActivity
import com.example.unihub.R
import com.example.unihub.data.repository.NotificationHistoryRepository
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class TaskDeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val identifier = intent.getStringExtra(EXTRA_TASK_IDENTIFIER) ?: return
        val titulo = intent.getStringExtra(EXTRA_TASK_TITLE)
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.task_notification_label)
        val board = intent.getStringExtra(EXTRA_TASK_BOARD)
            ?.takeIf { it.isNotBlank() }
        val deadlineIso = intent.getStringExtra(EXTRA_TASK_DEADLINE_ISO)
        val deadlineMillis = intent.getLongExtra(EXTRA_TASK_DEADLINE_MILLIS, -1L)
            .takeIf { it > 0L }

        createChannel(context)

        val notificationId = abs(identifier.hashCode())

        val visualizarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val visualizarPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(visualizarIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        val deadlineDateTime = resolveDeadlineDateTime(deadlineMillis, deadlineIso)
        val deadlineText = deadlineDateTime?.let { formatDeadline(context, it) }
            ?: context.getString(R.string.task_notification_deadline_unknown)
        val boardText = board?.let { context.getString(R.string.task_notification_board_format, it) }
        val promptText = context.getString(R.string.task_notification_prompt)

        val summaryText = listOfNotNull(deadlineText.takeUnless { it.isBlank() }, boardText)
            .joinToString(" • ")
        val historyMessage = listOfNotNull(promptText, deadlineText, boardText)
            .filter { it.isNotBlank() }
            .joinToString("\n")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(summaryText.ifBlank { promptText })
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(visualizarPendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(historyMessage))

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())

        NotificationHistoryRepository.getInstance(context.applicationContext)
            .logNotification(
                title = titulo,
                message = historyMessage,
                timestampMillis = System.currentTimeMillis()
            )
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.task_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.task_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun resolveDeadlineDateTime(
        deadlineMillis: Long?,
        deadlineIso: String?
    ): ZonedDateTime? {
        val zone = ZoneId.systemDefault()
        return when {
            deadlineMillis != null -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(deadlineMillis), zone)
            deadlineIso.isNullOrBlank() -> null
            else -> TaskDeadlineNotificationScheduler.parseDeadline(deadlineIso, zone)
        }
    }

    private fun formatDeadline(context: Context, deadline: ZonedDateTime): String {
        val locale = Locale("pt", "BR")
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", locale)
        return context.getString(R.string.task_notification_deadline_format, deadline.format(formatter))
    }


    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        const val EXTRA_TASK_IDENTIFIER = "extra_task_identifier"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_BOARD = "extra_task_board"
        const val EXTRA_TASK_DEADLINE_ISO = "extra_task_deadline_iso"
        const val EXTRA_TASK_DEADLINE_MILLIS = "extra_task_deadline_millis"

        private const val CHANNEL_ID = "task_deadline_reminders"
    }
}

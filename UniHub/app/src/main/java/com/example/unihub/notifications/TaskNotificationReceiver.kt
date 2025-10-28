package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.unihub.MainActivity
import com.example.unihub.R
import com.example.unihub.data.repository.NotificationHistoryRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val identifierSeed = intent.getStringExtra(EXTRA_TASK_IDENTIFIER).orEmpty()
        if (identifierSeed.isBlank()) return

        val titulo = intent.getStringExtra(EXTRA_TASK_TITLE).orEmpty()
        val descricao = intent.getStringExtra(EXTRA_TASK_DESCRIPTION).orEmpty()
        val deadlineIso = intent.getStringExtra(EXTRA_TASK_DEADLINE_ISO)
        val quadroNome = intent.getStringExtra(EXTRA_TASK_BOARD_NAME)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        createChannel(context)

        val notificationId = abs((identifierSeed + (deadlineIso ?: "")).hashCode())

        val visualizarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val visualizarPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(visualizarIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or TaskNotificationScheduler.immutableFlag(),
            )
        }

        val deadlineText = formatDeadline(context, deadlineIso)
        val promptText = context.getString(R.string.task_notification_prompt)
        val boardText = quadroNome?.takeIf { it.isNotBlank() }
            ?.let { context.getString(R.string.task_notification_board_format, it) }
            .orEmpty()

        val locale = Locale("pt", "BR")
        val notificationTitle = titulo
            .takeIf { it.isNotBlank() }
            ?.uppercase(locale)
            ?: context.getString(R.string.task_notification_label)

        val summaryText = listOf(deadlineText, boardText)
            .filter { it.isNotBlank() }
            .joinToString(separator = " • ")

        val historyMessage = listOfNotNull(
            promptText,
            deadlineText.takeIf { it.isNotBlank() },
            boardText.takeIf { it.isNotBlank() },
        ).joinToString("\n")

        val contentView = RemoteViews(context.packageName, R.layout.notification_attendance).apply {
            setTextViewText(
                R.id.notification_app_title,
                context.getString(R.string.task_notification_label),
            )
            setTextViewText(R.id.notification_title, notificationTitle)
            setTextViewText(
                R.id.notification_message,
                descricao.takeIf { it.isNotBlank() } ?: promptText,
            )
            setTextViewText(R.id.notification_time, deadlineText)

            if (boardText.isNotEmpty()) {
                setTextViewText(R.id.notification_absence_info, boardText)
                setViewVisibility(R.id.notification_absence_info, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_absence_info, View.GONE)
            }

            setTextViewText(
                R.id.notification_button_presence,
                context.getString(R.string.task_notification_button_details),
            )
            setViewVisibility(R.id.notification_button_presence, View.VISIBLE)
            setOnClickPendingIntent(R.id.notification_button_presence, visualizarPendingIntent)

            setViewVisibility(R.id.notification_button_absence, View.GONE)
            setOnClickPendingIntent(R.id.notification_root, visualizarPendingIntent)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(summaryText.ifBlank { promptText })
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(visualizarPendingIntent)
            .setCustomContentView(contentView)
            .setCustomBigContentView(contentView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        if (boardText.isNotEmpty()) {
            builder.setSubText(boardText)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())

        NotificationHistoryRepository.getInstance(context.applicationContext)
            .logNotification(
                title = notificationTitle,
                message = historyMessage,
                timestampMillis = System.currentTimeMillis(),
            )

        if (requestCode != -1) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or TaskNotificationScheduler.immutableFlag(),
            )
            pendingIntent?.let { alarmManager?.cancel(it) }
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.task_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.task_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun formatDeadline(context: Context, deadlineIso: String?): String {
        val zoned = TaskNotificationScheduler.parseDateTime(deadlineIso)
            ?: return context.getString(R.string.task_notification_deadline_unknown)

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
            .withZone(ZoneId.systemDefault())
        return context.getString(
            R.string.task_notification_deadline_format,
            formatter.format(zoned),
        )
    }

    companion object {
        const val EXTRA_TASK_IDENTIFIER = "extra_task_identifier"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESCRIPTION = "extra_task_description"
        const val EXTRA_TASK_DEADLINE_ISO = "extra_task_deadline_iso"
        const val EXTRA_TASK_BOARD_NAME = "extra_task_board_name"
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        private const val CHANNEL_ID = "task_notifications"
    }
}
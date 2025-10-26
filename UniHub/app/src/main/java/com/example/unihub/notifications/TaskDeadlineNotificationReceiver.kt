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
import kotlin.math.abs
import java.util.Locale

class TaskDeadlineNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val titulo = intent.getStringExtra(EXTRA_TAREFA_TITULO).orEmpty()
        if (titulo.isBlank()) return

        val prazoIso = intent.getStringExtra(EXTRA_TAREFA_PRAZO)
        val nomeQuadro = intent.getStringExtra(EXTRA_TAREFA_QUADRO)
        val identifier = intent.getStringExtra(EXTRA_IDENTIFIER).orEmpty()
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        createChannel(context)

        val notificationId = abs((identifier.ifEmpty { titulo } + (prazoIso ?: "")).hashCode())

        val abrirIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val abrirPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(abrirIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or AttendanceNotificationScheduler.immutableFlag()
            )
        }

        val promptText = context.getString(R.string.task_notification_prompt)
        val deadlineText = TaskDeadlineNotificationScheduler.formatDeadlineForDisplay(context, prazoIso)
        val quadroText = nomeQuadro?.takeIf { it.isNotBlank() }
            ?.let { context.getString(R.string.task_notification_board_format, it) }
            .orEmpty()

        val locale = Locale("pt", "BR")
        val notificationTitle = titulo.uppercase(locale)
        val summaryText = listOf(deadlineText, quadroText)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
        val historyMessage = listOf(promptText, deadlineText, quadroText.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString("\n")

        val contentView = RemoteViews(context.packageName, R.layout.notification_attendance).apply {
            setTextViewText(R.id.notification_app_title, context.getString(R.string.task_notification_label))
            setTextViewText(R.id.notification_title, notificationTitle)
            setTextViewText(R.id.notification_message, promptText)
            setTextViewText(R.id.notification_time, deadlineText)

            if (quadroText.isNotEmpty()) {
                setTextViewText(R.id.notification_absence_info, quadroText)
                setViewVisibility(R.id.notification_absence_info, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_absence_info, View.GONE)
            }

            setTextViewText(
                R.id.notification_button_presence,
                context.getString(R.string.task_notification_button_details)
            )
            setViewVisibility(R.id.notification_button_presence, View.VISIBLE)
            setOnClickPendingIntent(R.id.notification_button_presence, abrirPendingIntent)

            setViewVisibility(R.id.notification_button_absence, View.GONE)
            setOnClickPendingIntent(R.id.notification_root, abrirPendingIntent)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(summaryText.ifBlank { promptText })
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(abrirPendingIntent)
            .setCustomContentView(contentView)
            .setCustomBigContentView(contentView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())

        NotificationHistoryRepository.getInstance(context.applicationContext)
            .logNotification(
                title = notificationTitle,
                message = historyMessage,
                timestampMillis = System.currentTimeMillis()
            )

        if (requestCode != -1) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or AttendanceNotificationScheduler.immutableFlag()
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
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.task_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_TAREFA_TITULO = "extra_tarefa_titulo"
        const val EXTRA_TAREFA_PRAZO = "extra_tarefa_prazo"
        const val EXTRA_TAREFA_QUADRO = "extra_tarefa_quadro"
        const val EXTRA_IDENTIFIER = "extra_tarefa_identifier"
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        private const val CHANNEL_ID = "task_deadline_notifications"
    }
}
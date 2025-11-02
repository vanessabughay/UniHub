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
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val avaliacaoId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (avaliacaoId < 0) return

        val descricao = intent.getStringExtra(EXTRA_TASK_DESCRICAO).orEmpty()
        val disciplinaId = intent.getLongExtra(EXTRA_DISCIPLINA_ID, -1L).takeIf { it >= 0 }
        val disciplina = intent.getStringExtra(EXTRA_DISCIPLINA_NOME)
        val dataHoraIso = intent.getStringExtra(EXTRA_TASK_DATA_HORA)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        createChannel(context)

        val notificationId = abs((avaliacaoId.toString() + (dataHoraIso ?: "")).hashCode())

        val visualizarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_AVALIACAO_ID, avaliacaoId.toString())
            putExtra(MainActivity.EXTRA_TARGET_SCREEN, MainActivity.TARGET_SCREEN_VISUALIZAR_AVALIACAO)
            disciplinaId?.let {
                putExtra(MainActivity.EXTRA_TARGET_DISCIPLINA_ID, it.toString())
            }
        }

        val visualizarPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(visualizarIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or AttendanceNotificationScheduler.immutableFlag()
            )
        }

        val formattedDateTime = formatDateTimeForDisplay(context, dataHoraIso)
        val promptText = context.getString(R.string.task_notification_prompt)
        val disciplineText = disciplina?.takeIf { it.isNotBlank() }
            ?.let { context.getString(R.string.task_notification_discipline_format, it) }
            .orEmpty()

        val locale = Locale("pt", "BR")
        val notificationTitle = descricao.takeIf { it.isNotBlank() }
            ?.uppercase(locale)
            ?: context.getString(R.string.task_notification_default_title)

        val historyMessage = listOfNotNull(promptText, formattedDateTime, disciplineText.takeIf { it.isNotBlank() })
            .joinToString("\n")

        val contentView = RemoteViews(context.packageName, R.layout.notification_attendance).apply {
            setTextViewText(R.id.notification_app_title, context.getString(R.string.task_notification_label))
            setTextViewText(R.id.notification_title, notificationTitle)
            setTextViewText(R.id.notification_message, promptText)
            setTextViewText(R.id.notification_time, formattedDateTime)

            if (disciplineText.isNotEmpty()) {
                setTextViewText(R.id.notification_absence_info, disciplineText)
                setViewVisibility(R.id.notification_absence_info, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_absence_info, View.GONE)
            }

            setTextViewText(
                R.id.notification_button_presence,
                context.getString(R.string.task_notification_button_details)
            )
            setViewVisibility(R.id.notification_button_presence, View.VISIBLE)
            setOnClickPendingIntent(R.id.notification_button_presence, visualizarPendingIntent)

            setViewVisibility(R.id.notification_button_absence, View.GONE)
            setOnClickPendingIntent(R.id.notification_root, visualizarPendingIntent)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitle)
            .setContentText(formattedDateTime.ifBlank { promptText })
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(visualizarPendingIntent)
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

    private fun formatDateTimeForDisplay(context: Context, dateTimeString: String?): String {
        val zoned = TaskNotificationScheduler.parseDateTime(dateTimeString)
            ?: return context.getString(R.string.task_notification_schedule_unknown)

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
        return context.getString(
            R.string.task_notification_schedule_format,
            formatter.format(zoned)
        )
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_DESCRICAO = "extra_task_descricao"
        const val EXTRA_DISCIPLINA_ID = "extra_disciplina_id"
        const val EXTRA_DISCIPLINA_NOME = "extra_disciplina_nome"
        const val EXTRA_TASK_DATA_HORA = "extra_task_data_hora"
        const val EXTRA_REQUEST_CODE = "extra_request_code"

        private const val CHANNEL_ID = "task_notifications"
    }
}
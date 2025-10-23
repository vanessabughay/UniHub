package com.example.unihub.notifications

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
import kotlin.math.abs
import java.util.Locale

class AttendanceNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val disciplinaId = intent.getLongExtra(EXTRA_DISCIPLINA_ID, -1L)
        val disciplinaNome = intent.getStringExtra(EXTRA_DISCIPLINA_NOME) ?: return
        val dia = intent.getStringExtra(EXTRA_AULA_DIA).orEmpty()
        val inicio = intent.getIntExtra(EXTRA_AULA_INICIO, -1)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)
        val totalAusencias = intent.getIntExtra(EXTRA_TOTAL_AUSENCIAS, NO_VALUE)
            .takeIf { it >= 0 }
        val limiteAusencias = intent.getIntExtra(EXTRA_AUSENCIAS_MAX, NO_AUSENCIA_LIMIT)
            .takeIf { it >= 0 }

        createChannel(context)

        val notificationId = abs((disciplinaId.toString() + inicio.toString()).hashCode())

        val visualizarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_DISCIPLINA_ID, disciplinaId.toString())
            putExtra(MainActivity.EXTRA_TARGET_SCREEN, MainActivity.TARGET_SCREEN_VISUALIZAR_DISCIPLINA)
        }

        val visualizarPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(visualizarIntent)
            getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        val ausenciaIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TARGET_DISCIPLINA_ID, disciplinaId.toString())
            putExtra(MainActivity.EXTRA_TARGET_SCREEN, MainActivity.TARGET_SCREEN_REGISTRAR_AUSENCIA)
        }

        val ausenciaPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(ausenciaIntent)
            getPendingIntent(
                notificationId + 1,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        val presencaPendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(visualizarIntent)
            getPendingIntent(
                notificationId + 2,
                PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
            )
        }

        val formattedTime = formatMinutes(inicio)
        val scheduleText = buildScheduleText(context, dia, formattedTime)
        val absenceInfoText = buildAbsenceInfoText(context, totalAusencias, limiteAusencias)
        val promptText = context.getString(R.string.attendance_notification_prompt)

        val locale = Locale("pt", "BR")
        val notificationTitle = "${disciplinaNome.uppercase(locale)}"
        val summaryText = buildSummaryText(scheduleText, absenceInfoText)

        val contentView = RemoteViews(context.packageName, R.layout.notification_attendance).apply {
            setTextViewText(R.id.notification_title, notificationTitle)
            setTextViewText(R.id.notification_message, promptText)
            setTextViewText(R.id.notification_time, scheduleText)
            if (absenceInfoText.isNotEmpty()) {
                setTextViewText(R.id.notification_absence_info, absenceInfoText)
                setViewVisibility(R.id.notification_absence_info, View.VISIBLE)
            } else {
                setViewVisibility(R.id.notification_absence_info, View.GONE)
            }
            setOnClickPendingIntent(R.id.notification_root, visualizarPendingIntent)
            setOnClickPendingIntent(R.id.notification_button_presence, presencaPendingIntent)
            setOnClickPendingIntent(R.id.notification_button_absence, ausenciaPendingIntent)
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

        if (absenceInfoText.isNotEmpty()) {
            builder.setSubText(absenceInfoText)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())


        if (requestCode != -1) {
            val nextTrigger = AttendanceNotificationScheduler.computeTriggerMillis(dia, inicio)
            if (nextTrigger != null) {
                val nextIntent = Intent(context, AttendanceNotificationReceiver::class.java).apply {
                    putExtras(intent)
                }
                AttendanceNotificationScheduler(context)
                    .scheduleExactAlarm(requestCode, nextTrigger, nextIntent)
            }
        }
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.attendance_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.attendance_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun formatMinutes(totalMinutes: Int): String {
        if (totalMinutes < 0) return ""
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    private fun buildScheduleText(context: Context, dayLabel: String, formattedTime: String): String {
        val locale = Locale("pt", "BR")
        val normalizedDay = dayLabel.trim()
            .takeIf { it.isNotEmpty() }
            ?.lowercase(locale)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

        return when {
            normalizedDay != null && formattedTime.isNotEmpty() ->
                context.getString(R.string.attendance_notification_schedule_format, normalizedDay, formattedTime)

            normalizedDay != null -> normalizedDay
            formattedTime.isNotEmpty() ->
                context.getString(R.string.attendance_notification_time_only_format, formattedTime)
            else -> context.getString(R.string.attendance_notification_schedule_unknown)
        }
    }

    private fun buildAbsenceInfoText(
        context: Context,
        totalAusencias: Int?,
        limiteAusencias: Int?
    ): String {
        return when {
            totalAusencias != null && limiteAusencias != null ->
                context.getString(
                    R.string.attendance_notification_absence_with_limit,
                    totalAusencias,
                    limiteAusencias
                )

            totalAusencias != null ->
                context.getString(
                    R.string.attendance_notification_absence_without_limit,
                    totalAusencias
                )

            limiteAusencias != null ->
                context.getString(
                    R.string.attendance_notification_absence_only_limit,
                    limiteAusencias
                )

            else -> ""
        }
    }

    private fun buildSummaryText(scheduleText: String, absenceInfoText: String): String {
        return listOf(scheduleText, absenceInfoText)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
    }


    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        const val EXTRA_DISCIPLINA_ID = "extra_disciplina_id"
        const val EXTRA_DISCIPLINA_NOME = "extra_disciplina_nome"
        const val EXTRA_AULA_DIA = "extra_aula_dia"
        const val EXTRA_AULA_INICIO = "extra_aula_inicio"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
        const val EXTRA_TOTAL_AUSENCIAS = "extra_total_ausencias"
        const val EXTRA_AUSENCIAS_MAX = "extra_ausencias_max"

        const val NO_AUSENCIA_LIMIT = -1
        private const val NO_VALUE = -1

        private const val CHANNEL_ID = "attendance_reminders"
    }
}

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
import com.example.unihub.components.formatMinutesToTime
import com.example.unihub.data.config.TokenManager
import com.example.unihub.data.repository.NotificationHistoryRepository
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

class FrequenciaNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Opcional: se quiser bloquear notificações sem login
        TokenManager.loadToken(context.applicationContext)
//        if (TokenManager.token.isNullOrBlank()) {
//            return
//        }

        val disciplinaId = intent.getLongExtra(EXTRA_DISCIPLINA_ID, -1L)
        val disciplinaNome = intent.getStringExtra(EXTRA_DISCIPLINA_NOME) ?: return
        val dia = intent.getStringExtra(EXTRA_AULA_DIA).orEmpty()
        val inicio = intent.getIntExtra(EXTRA_AULA_INICIO, -1)
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, -1)

        // ✅ Guard clause: se algo essencial veio inválido, aborta
        if (disciplinaId <= 0L || inicio < 0 || requestCode < 0) {
            return
        }

        val totalAusencias = intent.getIntExtra(EXTRA_TOTAL_AUSENCIAS, NO_VALUE)
            .takeIf { it >= 0 }
        val limiteAusencias = intent.getIntExtra(EXTRA_AUSENCIAS_MAX, NO_AUSENCIA_LIMIT)
            .takeIf { it >= 0 }
        val occurrenceEpochDay = intent.getLongExtra(EXTRA_AULA_EPOCH_DAY, Long.MIN_VALUE)
            .takeIf { it != Long.MIN_VALUE }
            ?: Instant.ofEpochMilli(System.currentTimeMillis())
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toEpochDay()

        createChannel(context)

        val notificationId = abs((disciplinaId.toString() + inicio.toString()).hashCode())
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId)

        val historyRepository = NotificationHistoryRepository.getInstance(context.applicationContext)
        val referenceId = NotificationHistoryRepository.buildFrequenciaReferenceId(
            disciplinaId,
            occurrenceEpochDay
        )

        if (!historyRepository.shouldNotifyFrequencia(referenceId)) {
            scheduleNextOccurrence(context, intent, requestCode, dia, inicio)
            return
        }

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

        val presencaIntent = Intent(context, FrequenciaNotificationActionReceiver::class.java).apply {
            action = FrequenciaNotificationActionReceiver.ACTION_MARK_PRESENCE
            putExtra(FrequenciaNotificationActionReceiver.EXTRA_DISCIPLINA_ID, disciplinaId)
            putExtra(FrequenciaNotificationActionReceiver.EXTRA_DISCIPLINA_NOME, disciplinaNome)
            putExtra(FrequenciaNotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(FrequenciaNotificationActionReceiver.EXTRA_OCCURRENCE_EPOCH_DAY, occurrenceEpochDay)
        }

        val presencaPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 2,
            presencaIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        val formattedTime = formatMinutes(inicio)
        val scheduleText = buildScheduleText(context, dia, formattedTime)
        val absenceInfoText = buildAbsenceInfoText(context, totalAusencias, limiteAusencias)
        val promptText = context.getString(R.string.frequencia_notification_prompt)

        val locale = Locale("pt", "BR")
        val notificationTitle = disciplinaNome.uppercase(locale)
        val summaryText = buildSummaryText(scheduleText, absenceInfoText)
        val historyMessage = listOf(promptText, summaryText.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString("\n")

        val contentView = RemoteViews(context.packageName, R.layout.notificacao_frequencia).apply {
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

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (se: SecurityException) {

        }


        historyRepository.logNotification(
            title = notificationTitle,
            message = historyMessage,
            timestampMillis = System.currentTimeMillis(),
            type = NotificationHistoryRepository.FREQUENCIA_TYPE,
            category = NotificationHistoryRepository.FREQUENCIA_CATEGORY,
            referenceId = referenceId,
            hasPendingInteraction = true,
            metadata = mapOf(
                "disciplinaId" to disciplinaId,
                "occurrenceEpochDay" to occurrenceEpochDay,
                "requestCode" to requestCode,
                "dia" to dia,
                "inicio" to inicio,
                "notificationId" to notificationId
            )
        )

        scheduleNextOccurrence(context, intent, requestCode, dia, inicio)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.frequencia_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.frequencia_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private fun formatMinutes(totalMinutes: Int): String {
        if (totalMinutes < 0) return ""
        return formatMinutesToTime(totalMinutes)
    }

    private fun buildScheduleText(context: Context, dayLabel: String, formattedTime: String): String {
        val locale = Locale("pt", "BR")
        val normalizedDay = dayLabel.trim()
            .takeIf { it.isNotEmpty() }
            ?.lowercase(locale)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

        return when {
            normalizedDay != null && formattedTime.isNotEmpty() ->
                context.getString(R.string.frequencia_notification_schedule_format, normalizedDay, formattedTime)

            normalizedDay != null -> normalizedDay
            formattedTime.isNotEmpty() ->
                context.getString(R.string.frequencia_notification_time_only_format, formattedTime)
            else -> context.getString(R.string.frequencia_notification_schedule_unknown)
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
                    R.string.frequencia_notification_absence_with_limit,
                    totalAusencias,
                    limiteAusencias
                )

            totalAusencias != null ->
                context.getString(
                    R.string.frequencia_notification_absence_without_limit,
                    totalAusencias
                )

            limiteAusencias != null ->
                context.getString(
                    R.string.frequencia_notification_absence_only_limit,
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

    private fun scheduleNextOccurrence(
        context: Context,
        intent: Intent,
        requestCode: Int,
        dia: String,
        inicio: Int
    ) {
        if (requestCode == -1) return
        val nextTrigger = FrequenciaNotificationScheduler.computeTriggerMillis(dia, inicio)
        if (nextTrigger != null) {
            val nextIntent = Intent(context, FrequenciaNotificationReceiver::class.java).apply {
                putExtras(intent)
                putExtra(
                    EXTRA_AULA_EPOCH_DAY,
                    Instant.ofEpochMilli(nextTrigger)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toEpochDay()
                )
            }
            FrequenciaNotificationScheduler(context)
                .scheduleExactAlarm(requestCode, nextTrigger, nextIntent)
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
        const val EXTRA_AULA_EPOCH_DAY = "extra_aula_epoch_day"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        const val NO_AUSENCIA_LIMIT = -1
        private const val NO_VALUE = -1

        private const val CHANNEL_ID = "frequencia_reminders"
    }
}

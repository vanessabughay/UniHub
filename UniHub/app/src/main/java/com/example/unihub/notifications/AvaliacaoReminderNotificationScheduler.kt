package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
// MUDANÇA AQUI: Importa o java.time.Duration
import com.example.unihub.data.model.Prioridade
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import kotlin.math.abs

class AvaliacaoNotificationScheduler(private val context: Context) {

    data class AvaliacaoInfo(
        val id: Long,
        val descricao: String?,
        val disciplinaId: Long?,
        val disciplinaNome: String?,
        val dataHoraIso: String?,
        val reminderDuration: Duration,
        val receberNotificacoes: Boolean
    )

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun scheduleNotifications(avaliacoes: List<AvaliacaoInfo>) {
        val manager = alarmManager ?: return

        val storedRequestCodes =
            preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)

        if (!canScheduleExactAlarms(manager)) {
            preferences.edit().remove(KEY_REQUEST_CODES).apply()
            return
        }

        val newRequestCodes = mutableSetOf<String>()
        val baseIntent = Intent(context, AvaliacaoNotificationReceiver::class.java)

        avaliacoes
            .filter { it.receberNotificacoes }
            .forEach { avaliacao ->
                val triggerAtMillis = computeTriggerMillis(
                    avaliacao.dataHoraIso,
                    avaliacao.reminderDuration
                )
                    ?: return@forEach

                val requestCode = buildRequestCode(avaliacao.id, triggerAtMillis)
                newRequestCodes.add(requestCode.toString())

                val intent = Intent(baseIntent).apply {
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_AVALIACAO_ID, avaliacao.id)
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_AVALIACAO_DESCRICAO, avaliacao.descricao)
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_DISCIPLINA_ID, avaliacao.disciplinaId)
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_DISCIPLINA_NOME, avaliacao.disciplinaNome)
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_AVALIACAO_DATA_HORA, avaliacao.dataHoraIso)
                    putExtra(AvaliacaoNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
                }

                scheduleExactAlarm(requestCode, triggerAtMillis, intent)
            }

        preferences.edit().putStringSet(KEY_REQUEST_CODES, newRequestCodes).apply()
    }

    private fun cancelStored(manager: AlarmManager, storedCodes: Set<String>) {
        if (storedCodes.isEmpty()) return
        storedCodes.mapNotNull { it.toIntOrNull() }
            .forEach { code ->
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    Intent(context, AvaliacaoNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
                )
                manager.cancel(pendingIntent)
            }
    }

    fun scheduleExactAlarm(requestCode: Int, triggerAtMillis: Long, intent: Intent) {
        val manager = alarmManager ?: return

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
        )

        if (!canScheduleExactAlarms(manager)) {
            return
        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                manager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            else -> {
                manager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    private fun buildRequestCode(id: Long, triggerAtMillis: Long): Int {
        val raw = Objects.hash(id, triggerAtMillis)
        return abs(raw.takeIf { it != Int.MIN_VALUE } ?: 0)
    }

    private fun canScheduleExactAlarms(manager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    companion object {
        private const val PREFS_NAME = "avaliacao_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        private val LOCAL_DATE_TIME_FORMATTERS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )

        internal fun computeTriggerMillis(
            dateTimeString: String?,
            reminderDuration: Duration,
            zoneId: ZoneId = ZoneId.systemDefault(),
            now: ZonedDateTime = ZonedDateTime.now(zoneId)
        ): Long? {
            val zonedDateTime = parseToZonedDateTime(dateTimeString, zoneId) ?: return null

            // Lógica alterada para usar a Duration recebida
            val reminderDateTime = zonedDateTime.minus(reminderDuration)

            if (!reminderDateTime.isAfter(now)) {
                return null
            }
            return reminderDateTime.toInstant().toEpochMilli()
        }


        internal fun parseDateTime(
            dateTimeString: String?,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): ZonedDateTime? = parseToZonedDateTime(dateTimeString, zoneId)

        private fun parseToZonedDateTime(
            value: String?,
            zoneId: ZoneId
        ): ZonedDateTime? {
            val trimmed = value?.trim().orEmpty()
            if (trimmed.isEmpty()) return null

            runCatching { Instant.parse(trimmed) }.getOrNull()?.let {
                return it.atZone(zoneId)
            }

            runCatching { ZonedDateTime.parse(trimmed) }.getOrNull()?.let { return it }
            runCatching { java.time.OffsetDateTime.parse(trimmed) }.getOrNull()?.let {
                return it.atZoneSameInstant(zoneId)
            }

            LOCAL_DATE_TIME_FORMATTERS.forEach { formatter ->
                runCatching { LocalDateTime.parse(trimmed, formatter) }.getOrNull()?.let {
                    return it.atZone(zoneId)
                }
            }

            runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()?.let {
                return it.atStartOfDay(zoneId)
            }

            return null
        }

        internal fun immutableFlag(): Int = FrequenciaNotificationScheduler.immutableFlag()

        fun defaultReminderDuration(priority: Prioridade?): Duration {
            return when (priority) {
                Prioridade.MUITO_BAIXA -> Duration.ofHours(3)
                Prioridade.BAIXA -> Duration.ofHours(12)
                Prioridade.MEDIA -> Duration.ofDays(1)
                Prioridade.ALTA -> Duration.ofDays(2)
                Prioridade.MUITO_ALTA -> Duration.ofDays(3)
                null -> Duration.ofDays(1)
            }
        }
    }
}

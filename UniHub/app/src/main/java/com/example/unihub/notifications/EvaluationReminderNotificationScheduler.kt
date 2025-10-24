package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

class EvaluationNotificationScheduler(private val context: Context) {

    data class EvaluationInfo(
        val id: Long,
        val descricao: String?,
        val disciplinaId: Long?,
        val disciplinaNome: String?,
        val dataHoraIso: String?,
        val prioridade: Prioridade?,
        val receberNotificacoes: Boolean
    )

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun scheduleNotifications(avaliacoes: List<EvaluationInfo>) {
        val manager = alarmManager ?: return

        val storedRequestCodes =
            preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)

        if (!canScheduleExactAlarms(manager)) {
            preferences.edit().remove(KEY_REQUEST_CODES).apply()
            return
        }

        val newRequestCodes = mutableSetOf<String>()
        val baseIntent = Intent(context, EvaluationNotificationReceiver::class.java)

        avaliacoes
            .filter { it.receberNotificacoes }
            .forEach { avaliacao ->
                val triggerAtMillis = computeTriggerMillis(
                    avaliacao.dataHoraIso,
                    avaliacao.prioridade
                )
                    ?: return@forEach

                val requestCode = buildRequestCode(avaliacao.id, triggerAtMillis)
                newRequestCodes.add(requestCode.toString())

                val intent = Intent(baseIntent).apply {
                    putExtra(EvaluationNotificationReceiver.EXTRA_AVALIACAO_ID, avaliacao.id)
                    putExtra(EvaluationNotificationReceiver.EXTRA_AVALIACAO_DESCRICAO, avaliacao.descricao)
                    putExtra(EvaluationNotificationReceiver.EXTRA_DISCIPLINA_ID, avaliacao.disciplinaId)
                    putExtra(EvaluationNotificationReceiver.EXTRA_DISCIPLINA_NOME, avaliacao.disciplinaNome)
                    putExtra(EvaluationNotificationReceiver.EXTRA_AVALIACAO_DATA_HORA, avaliacao.dataHoraIso)
                    putExtra(EvaluationNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
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
                    Intent(context, EvaluationNotificationReceiver::class.java),
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
        private const val PREFS_NAME = "evaluation_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        private val LOCAL_DATE_TIME_FORMATTERS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )

        internal fun computeTriggerMillis(
            dateTimeString: String?,
            prioridade: Prioridade?,
            zoneId: ZoneId = ZoneId.systemDefault(),
            now: ZonedDateTime = ZonedDateTime.now(zoneId)
        ): Long? {
            val zonedDateTime = parseToZonedDateTime(dateTimeString, zoneId) ?: return null
            val reminderDateTime = zonedDateTime.minus(toReminderDuration(prioridade))
            if (!reminderDateTime.isAfter(now)) {
                return null
            }
            return reminderDateTime.toInstant().toEpochMilli()
        }

        private fun toReminderDuration(prioridade: Prioridade?): Duration {
            return when (prioridade) {
                Prioridade.MUITO_BAIXA -> Duration.ofHours(3)
                Prioridade.BAIXA -> Duration.ofHours(12)
                Prioridade.MEDIA -> Duration.ofHours(48)
                Prioridade.ALTA -> Duration.ofDays(5)
                Prioridade.MUITO_ALTA -> Duration.ofDays(7)
                null -> Duration.ofHours(48)
            }
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

        private fun immutableFlag(): Int = AttendanceNotificationScheduler.immutableFlag()
    }
}
package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import kotlin.math.abs

class TaskNotificationScheduler(private val context: Context) {

    data class TaskInfo(
        val identifierSeed: String,
        val titulo: String,
        val descricao: String?,
        val deadlineIso: String?,
        val quadroNome: String?,
        val receberNotificacoes: Boolean,
    )

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun scheduleNotifications(tarefas: List<TaskInfo>) {
        val manager = alarmManager ?: return

        val storedRequestCodes =
            preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)

        if (!canScheduleExactAlarms(manager)) {
            preferences.edit().remove(KEY_REQUEST_CODES).apply()
            return
        }

        val newRequestCodes = mutableSetOf<String>()
        val baseIntent = Intent(context, TaskNotificationReceiver::class.java)

        tarefas
            .filter { it.receberNotificacoes }
            .forEach { tarefa ->
                val triggerAtMillis = computeTriggerMillis(tarefa.deadlineIso) ?: return@forEach

                val requestCode = buildRequestCode(tarefa.identifierSeed, triggerAtMillis)
                newRequestCodes.add(requestCode.toString())

                val intent = Intent(baseIntent).apply {
                    putExtra(TaskNotificationReceiver.EXTRA_TASK_IDENTIFIER, tarefa.identifierSeed)
                    putExtra(TaskNotificationReceiver.EXTRA_TASK_TITLE, tarefa.titulo)
                    putExtra(TaskNotificationReceiver.EXTRA_TASK_DESCRIPTION, tarefa.descricao)
                    putExtra(TaskNotificationReceiver.EXTRA_TASK_DEADLINE_ISO, tarefa.deadlineIso)
                    putExtra(TaskNotificationReceiver.EXTRA_TASK_BOARD_NAME, tarefa.quadroNome)
                    putExtra(TaskNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
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
                    Intent(context, TaskNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag(),
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
            PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag(),
        )

        if (!canScheduleExactAlarms(manager)) {
            return
        }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                manager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }

            else -> {
                manager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        }
    }

    private fun buildRequestCode(identifierSeed: String, triggerAtMillis: Long): Int {
        val raw = Objects.hash(identifierSeed, triggerAtMillis)
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
        private const val PREFS_NAME = "task_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        private val LOCAL_DATE_TIME_FORMATTERS = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )

        internal fun computeTriggerMillis(
            deadlineIso: String?,
            zoneId: ZoneId = ZoneId.systemDefault(),
            now: ZonedDateTime = ZonedDateTime.now(zoneId),
        ): Long? {
            val dateTime = parseToZonedDateTime(deadlineIso, zoneId) ?: return null
            if (dateTime.isBefore(now)) {
                return null
            }
            return dateTime.toInstant().toEpochMilli()
        }

        internal fun parseDateTime(
            deadlineIso: String?,
            zoneId: ZoneId = ZoneId.systemDefault(),
        ): ZonedDateTime? = parseToZonedDateTime(deadlineIso, zoneId)

        private fun parseToZonedDateTime(
            value: String?,
            zoneId: ZoneId,
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
                runCatching { LocalDateTime.parse(trimmed, formatter) }
                    .getOrNull()
                    ?.let { return it.atZone(zoneId) }
            }

            return runCatching {
                LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
                    .atStartOfDay(zoneId)
            }.getOrNull()
        }

        @Suppress("DEPRECATION")
        internal fun immutableFlag(): Int {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        }

        @VisibleForTesting
        fun clearPreferences(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }
}
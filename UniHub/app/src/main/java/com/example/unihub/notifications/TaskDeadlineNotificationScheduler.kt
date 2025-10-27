package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Objects
import kotlin.math.abs

class TaskDeadlineNotificationScheduler(private val context: Context) {

    data class TaskDeadlineInfo(
        val identifier: String,
        val titulo: String,
        val prazoIso: String,
        val nomeQuadro: String?
    )

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun scheduleNotifications(tarefas: List<TaskDeadlineInfo>) {
        val manager = alarmManager ?: return

        val storedRequestCodes =
            preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)

        if (!canScheduleExactAlarms(manager)) {
            preferences.edit().remove(KEY_REQUEST_CODES).apply()
            return
        }

        val newRequestCodes = mutableSetOf<String>()

        val baseIntent = Intent(context, TaskDeadlineNotificationReceiver::class.java)

        tarefas.forEach { tarefa ->
            val triggerAtMillis = computeTriggerMillis(tarefa.prazoIso) ?: return@forEach
            val requestCode = buildRequestCode(tarefa.identifier, triggerAtMillis)
            newRequestCodes.add(requestCode.toString())

            val intent = Intent(baseIntent).apply {
                putExtra(TaskDeadlineNotificationReceiver.EXTRA_TASK_IDENTIFIER, tarefa.identifier)
                putExtra(TaskDeadlineNotificationReceiver.EXTRA_TASK_TITLE, tarefa.titulo)
                putExtra(TaskDeadlineNotificationReceiver.EXTRA_TASK_BOARD, tarefa.nomeQuadro)
                putExtra(TaskDeadlineNotificationReceiver.EXTRA_TASK_DEADLINE_ISO, tarefa.prazoIso)
                putExtra(TaskDeadlineNotificationReceiver.EXTRA_TASK_DEADLINE_MILLIS, triggerAtMillis)
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
                    Intent(context, TaskDeadlineNotificationReceiver::class.java),
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

    private fun buildRequestCode(identifier: String, triggerAtMillis: Long): Int {
        val raw = Objects.hash(identifier, triggerAtMillis)
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
        private const val PREFS_NAME = "task_deadline_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        internal fun computeTriggerMillis(
            deadlineIso: String,
            now: ZonedDateTime = ZonedDateTime.now()
        ): Long? {
            val deadline = parseDeadline(deadlineIso, now.zone) ?: return null
            if (deadline.isBefore(now)) return null
            return deadline.toInstant().toEpochMilli()
        }

        @VisibleForTesting
        internal fun parseDeadline(
            deadlineIso: String,
            zone: ZoneId = ZoneId.systemDefault()
        ): ZonedDateTime? {
            val trimmed = deadlineIso.trim()
            if (trimmed.isEmpty()) return null

            val zoneAwareFormatters = listOf(
                DateTimeFormatter.ISO_ZONED_DATE_TIME,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )

            zoneAwareFormatters.forEach { formatter ->
                runCatching { ZonedDateTime.parse(trimmed, formatter) }
                    .getOrNull()
                    ?.let { return it }
            }

            val localDateTimeFormatters = listOf(

                DateTimeFormatter.ISO_LOCAL_DATE_TIME,

                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),

                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),

                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

            )

            localDateTimeFormatters.forEach { formatter ->

                runCatching { LocalDateTime.parse(trimmed, formatter) }

                    .getOrNull()

                    ?.let { return it.atZone(zone) }

            }

            return runCatching {

                LocalDate.parse(trimmed.take(10), DateTimeFormatter.ISO_LOCAL_DATE)

                    .atStartOfDay(zone)

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
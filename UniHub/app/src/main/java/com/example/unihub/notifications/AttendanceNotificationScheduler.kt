package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.example.unihub.data.model.HorarioAula
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.Objects
import kotlin.math.abs

class AttendanceNotificationScheduler(private val context: Context) {

    data class DisciplineScheduleInfo(
        val id: Long,
        val nome: String,
        val receberNotificacoes: Boolean,
        val horariosAulas: List<HorarioAula>
    )

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun scheduleNotifications(disciplinas: List<DisciplineScheduleInfo>) {
        val manager = alarmManager ?: return

        val storedRequestCodes =
            preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)

        val newRequestCodes = mutableSetOf<String>()

        val baseIntent = Intent(context, AttendanceNotificationReceiver::class.java)

        disciplinas.forEach { disciplina ->
            if (!disciplina.receberNotificacoes) {
                return@forEach
            }

            disciplina.horariosAulas.forEachIndexed { index, horario ->
                val triggerAtMillis = computeNextTriggerMillis(horario) ?: return@forEachIndexed

                val requestCode = buildRequestCode(disciplina.id, index, horario)
                newRequestCodes.add(requestCode.toString())

                val intent = Intent(baseIntent).apply {
                    putExtra(AttendanceNotificationReceiver.EXTRA_DISCIPLINA_ID, disciplina.id)
                    putExtra(AttendanceNotificationReceiver.EXTRA_DISCIPLINA_NOME, disciplina.nome)
                    putExtra(AttendanceNotificationReceiver.EXTRA_AULA_DIA, horario.diaDaSemana)
                    putExtra(AttendanceNotificationReceiver.EXTRA_AULA_INICIO, horario.horarioInicio)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
                )

                manager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
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
                    Intent(context, AttendanceNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
                )
                manager.cancel(pendingIntent)
            }
    }

    private fun computeNextTriggerMillis(horario: HorarioAula): Long? {
        val dayOfWeek = mapDayOfWeek(horario.diaDaSemana) ?: return null
        val startMinutes = horario.horarioInicio
        if (startMinutes < 0) return null

        val now = ZonedDateTime.now()
        var scheduled = now.with(TemporalAdjusters.nextOrSame(dayOfWeek))
            .withHour(startMinutes / 60)
            .withMinute(startMinutes % 60)
            .withSecond(0)
            .withNano(0)

        if (scheduled.isBefore(now)) {
            scheduled = scheduled.plusWeeks(1)
        }

        return scheduled.toInstant().toEpochMilli()
    }

    private fun mapDayOfWeek(label: String): DayOfWeek? {
        val normalized = label.trim().lowercase(Locale.getDefault())
        return when (normalized) {
            "segunda-feira" -> DayOfWeek.MONDAY
            "terça-feira", "terca-feira" -> DayOfWeek.TUESDAY
            "quarta-feira" -> DayOfWeek.WEDNESDAY
            "quinta-feira" -> DayOfWeek.THURSDAY
            "sexta-feira" -> DayOfWeek.FRIDAY
            "sábado", "sabado" -> DayOfWeek.SATURDAY
            "domingo" -> DayOfWeek.SUNDAY
            else -> null
        }
    }

    private fun buildRequestCode(id: Long, index: Int, horario: HorarioAula): Int {
        val raw = Objects.hash(id, index, horario.diaDaSemana, horario.horarioInicio)
        return abs(raw.takeIf { it != Int.MIN_VALUE } ?: 0)
    }

    @Suppress("DEPRECATION")
    private fun immutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    companion object {
        private const val PREFS_NAME = "attendance_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        @VisibleForTesting
        fun clearPreferences(context: Context) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        }
    }
}
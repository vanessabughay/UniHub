package com.example.unihub.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import com.example.unihub.data.model.HorarioAula
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.text.Normalizer
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.Objects
import kotlin.math.abs

class FrequenciaNotificationScheduler(private val context: Context) {

    data class DisciplineScheduleInfo(
        val id: Long,
        val nome: String,
        val receberNotificacoes: Boolean,
        val horariosAulas: List<HorarioAula>,
        val totalAusencias: Int,
        val ausenciasPermitidas: Int?
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

        if (!canScheduleExactAlarms(manager)) {
            preferences.edit().remove(KEY_REQUEST_CODES).apply()
            return
        }

        val newRequestCodes = mutableSetOf<String>()

        val baseIntent = Intent(context, FrequenciaNotificationReceiver::class.java)

        disciplinas.forEach { disciplina ->
            if (!disciplina.receberNotificacoes) {
                return@forEach
            }

            disciplina.horariosAulas.forEachIndexed { index, horario ->
                val triggerAtMillis = computeNextTriggerMillis(horario) ?: return@forEachIndexed

                val requestCode = buildRequestCode(disciplina.id, index, horario)
                newRequestCodes.add(requestCode.toString())

                val intent = Intent(baseIntent).apply {
                    putExtra(FrequenciaNotificationReceiver.EXTRA_DISCIPLINA_ID, disciplina.id)
                    putExtra(FrequenciaNotificationReceiver.EXTRA_DISCIPLINA_NOME, disciplina.nome)
                    putExtra(FrequenciaNotificationReceiver.EXTRA_AULA_DIA, horario.diaDaSemana)
                    putExtra(FrequenciaNotificationReceiver.EXTRA_AULA_INICIO, horario.horarioInicio)
                    putExtra(FrequenciaNotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
                    putExtra(FrequenciaNotificationReceiver.EXTRA_TOTAL_AUSENCIAS, disciplina.totalAusencias)
                    putExtra(
                        FrequenciaNotificationReceiver.EXTRA_AUSENCIAS_MAX,
                        disciplina.ausenciasPermitidas ?: FrequenciaNotificationReceiver.NO_AUSENCIA_LIMIT
                    )
                    putExtra(
                        FrequenciaNotificationReceiver.EXTRA_AULA_EPOCH_DAY,
                        computeEpochDay(triggerAtMillis)
                    )
                }

                scheduleExactAlarm(requestCode, triggerAtMillis, intent)
            }
        }

        preferences.edit().putStringSet(KEY_REQUEST_CODES, newRequestCodes).apply()
    }

    /**
     * Cancela todos os alarmes agendados e remove o registro das preferências.
     */
    fun cancelAllNotifications() {
        val manager = alarmManager ?: return
        val storedRequestCodes = preferences.getStringSet(KEY_REQUEST_CODES, emptySet()).orEmpty()
        cancelStored(manager, storedRequestCodes)
        preferences.edit().remove(KEY_REQUEST_CODES).apply()
    }


    private fun cancelStored(manager: AlarmManager, storedCodes: Set<String>) {
        if (storedCodes.isEmpty()) return
        storedCodes.mapNotNull { it.toIntOrNull() }
            .forEach { code ->
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    code,
                    Intent(context, FrequenciaNotificationReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag()
                )
                manager.cancel(pendingIntent)
            }
    }

    private fun computeNextTriggerMillis(horario: HorarioAula): Long? {
        return computeTriggerMillis(horario.diaDaSemana, horario.horarioInicio)
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

    private fun buildRequestCode(id: Long, index: Int, horario: HorarioAula): Int {
        val raw = Objects.hash(id, index, horario.diaDaSemana, horario.horarioInicio)
        return abs(raw.takeIf { it != Int.MIN_VALUE } ?: 0)
    }

    private fun computeEpochDay(triggerAtMillis: Long): Long {
        return Instant.ofEpochMilli(triggerAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    }

    private fun canScheduleExactAlarms(manager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            manager.canScheduleExactAlarms()
        } else {
            true
        }
    }


    companion object {
        private const val PREFS_NAME = "frequencia_notification_prefs"
        private const val KEY_REQUEST_CODES = "request_codes"

        internal fun computeTriggerMillis(
            dayLabel: String,
            startMinutes: Int,
            now: ZonedDateTime = ZonedDateTime.now()
        ): Long? {
            val dayOfWeek = mapDayOfWeek(dayLabel) ?: return null
            if (startMinutes < 0) return null

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

        private fun mapDayOfWeek(label: String): DayOfWeek? = mapDayOfWeekInternal(label)

        @VisibleForTesting
        internal fun mapDayOfWeekInternal(label: String): DayOfWeek? {
            val normalized = normalizeDayLabel(label)
            return when (normalized) {
                "segundafeira", "segunda" -> DayOfWeek.MONDAY
                "tercafeira", "terca" -> DayOfWeek.TUESDAY
                "quartafeira", "quarta" -> DayOfWeek.WEDNESDAY
                "quintafeira", "quinta" -> DayOfWeek.THURSDAY
                "sextafeira", "sexta" -> DayOfWeek.FRIDAY
                "sabado" -> DayOfWeek.SATURDAY
                else -> null
            }
        }

        @VisibleForTesting
        internal fun normalizeDayLabel(label: String): String {
            val trimmed = label.trim().lowercase(Locale.getDefault())
            val withoutDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            return withoutDiacritics.replace("[^a-z]".toRegex(), "")
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
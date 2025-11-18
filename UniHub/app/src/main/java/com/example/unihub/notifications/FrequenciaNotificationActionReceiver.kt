package com.example.unihub.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.example.unihub.R
import com.example.unihub.data.repository.NotificationHistoryRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FrequenciaNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != ACTION_MARK_PRESENCE) return

        val disciplinaId = intent.getLongExtra(EXTRA_DISCIPLINA_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val occurrenceEpochDay = intent.getLongExtra(EXTRA_OCCURRENCE_EPOCH_DAY, Long.MIN_VALUE)
        val disciplinaNome = intent.getStringExtra(EXTRA_DISCIPLINA_NOME)

        if (disciplinaId == -1L || occurrenceEpochDay == Long.MIN_VALUE) {
            if (notificationId != -1) {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
            return
        }

        val appContext = context.applicationContext
        val historyRepository = NotificationHistoryRepository.getInstance(appContext)
        val referenceId = NotificationHistoryRepository.buildFrequenciaReferenceId(
            disciplinaId,
            occurrenceEpochDay
        )

        val locale = Locale("pt", "BR")
        val title = disciplinaNome
            ?.takeIf { it.isNotBlank() }
            ?.uppercase(locale)
            ?: context.getString(R.string.frequencia_notification_history_title)

        val occurrenceDate = LocalDate.ofEpochDay(occurrenceEpochDay)
        val formattedDate = occurrenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val message = context.getString(
            R.string.frequencia_notification_history_presence,
            formattedDate
        )

        historyRepository.logFrequenciaResponse(
            referenceId = referenceId,
            title = title,
            message = message,
            metadata = mapOf(
                "disciplinaId" to disciplinaId,
                "occurrenceEpochDay" to occurrenceEpochDay,
                "response" to "PRESENCE",
                "notificationId" to notificationId.takeIf { it != -1 }
            ),
            syncWithBackend = false
        )

    }

    companion object {
        const val ACTION_MARK_PRESENCE = "com.example.unihub.action.MARCAR_FREQUENCIA_PRESENCA"
        const val EXTRA_DISCIPLINA_ID = "extra_disciplina_id"
        const val EXTRA_DISCIPLINA_NOME = "extra_disciplina_nome"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_OCCURRENCE_EPOCH_DAY = "extra_occurrence_epoch_day"
    }
}
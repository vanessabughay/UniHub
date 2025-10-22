package com.example.unihub.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.unihub.MainActivity
import com.example.unihub.R
import kotlin.math.abs

class AttendanceNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val disciplinaId = intent.getLongExtra(EXTRA_DISCIPLINA_ID, -1L)
        val disciplinaNome = intent.getStringExtra(EXTRA_DISCIPLINA_NOME) ?: return
        val dia = intent.getStringExtra(EXTRA_AULA_DIA).orEmpty()
        val inicio = intent.getIntExtra(EXTRA_AULA_INICIO, -1)

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

        val formattedTime = formatMinutes(inicio)
        val contentText = if (formattedTime.isNotEmpty()) {
            "${dia.ifBlank { "Sua aula" }} às $formattedTime. Registre sua presença ou ausência."
        } else {
            "Registre se esteve presente ou ausente nesta aula."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora da aula de $disciplinaNome")
            .setContentText(contentText)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(visualizarPendingIntent)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Marcar ausência",
                ausenciaPendingIntent
            )

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
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

        private const val CHANNEL_ID = "attendance_reminders"
    }
}

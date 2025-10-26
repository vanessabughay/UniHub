package com.example.unihub.components

import android.app.TimePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unihub.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CampoHorario(
    label: String,
    value: Int?,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecionar horário",
    is24Hour: Boolean = true,
    locale: Locale = Locale("pt", "BR"),
    defaultHour: Int = 12,
    defaultMinute: Int = 0,
) {
    val context = LocalContext.current

    val formattedValue = remember(value, locale, is24Hour) {
        value?.let { formatMinutesToTime(it, locale, is24Hour) } ?: ""
    }

    val displayText = if (formattedValue.isBlank()) placeholder else formattedValue
    val displayColor = if (formattedValue.isBlank()) {
        Color.Black.copy(alpha = 0.35f)
    } else {
        Color.Black.copy(alpha = 0.7f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                showLocalizedTimePicker(
                    context = context,
                    currentValue = value,
                    locale = locale,
                    is24Hour = is24Hour,
                    defaultHour = defaultHour,
                    defaultMinute = defaultMinute,
                    onTimeSelected = onTimeSelected,
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
            )
        )

        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 19.6.sp,
                color = displayColor,
            )
        )
    }
}

fun showLocalizedTimePicker(
    context: Context,
    currentValue: Int?,
    locale: Locale = Locale("pt", "BR"),
    is24Hour: Boolean = true,
    defaultHour: Int = 12,
    defaultMinute: Int = 0,
    onTimeSelected: (Int) -> Unit,
) {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)

    val localizedContext = ContextThemeWrapper(context, R.style.PickerTheme_Neutral).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            applyOverrideConfiguration(configuration)
        }
    }

    val originalLocale = Locale.getDefault()
    val originalLocaleList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.getDefault()
    } else {
        null
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.setDefault(LocaleList(locale))
    }
    Locale.setDefault(locale)

    val sanitizedValue = currentValue?.takeIf { it >= 0 }
    val hour = sanitizedValue?.div(60) ?: defaultHour
    val minute = sanitizedValue?.mod(60) ?: defaultMinute

    val restoreLocale = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            originalLocaleList?.let { LocaleList.setDefault(it) }
        }
        Locale.setDefault(originalLocale)
    }

    TimePickerDialog(
        localizedContext,
        { _, hourOfDay, minuteOfHour ->
            onTimeSelected(hourOfDay * 60 + minuteOfHour)
        },
        hour,
        minute,
        is24Hour
    ).apply {
        setOnDismissListener { restoreLocale() }
        show()
    }
}

fun formatMinutesToTime(
    totalMinutes: Int,
    locale: Locale = Locale("pt", "BR"),
    is24Hour: Boolean = true,
): String {
    if (totalMinutes < 0) return ""

    val normalizedMinutes = totalMinutes % (24 * 60)
    val hours = normalizedMinutes / 60
    val minutes = normalizedMinutes % 60

    return if (is24Hour) {
        String.format(locale, "%02d:%02d", hours, minutes)
    } else {
        val calendar = Calendar.getInstance(locale).apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
        }
        SimpleDateFormat("hh:mm a", locale).format(calendar.time)
    }
}
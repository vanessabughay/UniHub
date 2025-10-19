package com.example.unihub.components

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.view.ContextThemeWrapper
import com.example.unihub.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun showLocalizedDatePicker(
    context: Context,
    currentValue: Long,
    locale: Locale = Locale("pt", "BR"),
    onDateSelected: (Long) -> Unit
) {
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)

    val localizedContext = ContextThemeWrapper(context, R.style.Theme_UniHub).apply {
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

    val calendar = Calendar.getInstance(locale).apply {
        if (currentValue != 0L) {
            timeInMillis = currentValue
        }
    }

    val restoreLocale = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            originalLocaleList?.let { LocaleList.setDefault(it) }
        }
        Locale.setDefault(originalLocale)
    }

    DatePickerDialog(
        localizedContext,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance(locale).apply {
                set(year, month, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setOnDismissListener { restoreLocale() }
        show()
    }
}

fun formatDateToLocale(
    value: Long,
    locale: Locale = Locale("pt", "BR")
): String {
    if (value == 0L) return ""

    return SimpleDateFormat("dd/MM/yyyy (EEEE)", locale).format(Date(value))
}
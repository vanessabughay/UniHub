package com.example.unihub.ui.Calendario

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unihub.data.repository.AvaliacaoRepository
import com.example.unihub.data.repository.GoogleCalendarRepository

class CalendarioViewModelFactory(
    private val avaliacaoRepository: AvaliacaoRepository,
    private val googleCalendarRepository: GoogleCalendarRepository,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarioViewModel::class.java)) {
            return CalendarioViewModel(
                repository = avaliacaoRepository,
                calendarRepository = googleCalendarRepository,
                appContext = appContext.applicationContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
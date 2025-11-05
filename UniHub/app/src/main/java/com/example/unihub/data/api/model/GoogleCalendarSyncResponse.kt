package com.example.unihub.data.api.model

data class GoogleCalendarSyncResponse(
    val synced: Int,
    val failures: Int,
    val lastSyncedAt: String?
)
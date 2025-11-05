package com.example.unihub.data.api.model

data class GoogleCalendarStatusResponse(
    val linked: Boolean,
    val lastSyncedAt: String?,
    val requiresReauth: Boolean
)
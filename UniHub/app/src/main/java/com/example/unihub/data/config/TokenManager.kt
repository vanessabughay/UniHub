package com.example.unihub.data.config

import android.content.Context

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_EMAIL_KEY = "user_email"
    private const val USER_ID_KEY = "user_id"
    private const val CALENDAR_LINKED_KEY = "google_calendar_linked"
    private const val HAS_INSTITUTION_KEY = "has_institution"

    /** In‑memory representation of the token used by interceptors. */
    var token: String? = null
        private set

    var nomeUsuario: String? = null
        private set

    var emailUsuario: String? = null
        private set

    var usuarioId: Long? = null
        private set

    var googleCalendarLinked: Boolean = false
        private set

    var hasInstitution: Boolean = false
        private set

    /** Loads the token from shared preferences, if not already loaded. */
    fun loadToken(context: Context) {
        if (token == null || nomeUsuario == null || emailUsuario == null || usuarioId == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            token = prefs.getString(TOKEN_KEY, null)
            nomeUsuario = prefs.getString(USER_NAME_KEY, null)
            emailUsuario = prefs.getString(USER_EMAIL_KEY, null)
            usuarioId = prefs.takeIf { it.contains(USER_ID_KEY) }?.getLong(USER_ID_KEY, -1L)
                ?.takeIf { it != -1L }
            googleCalendarLinked = prefs.getBoolean(CALENDAR_LINKED_KEY, false)
            hasInstitution = prefs.getBoolean(HAS_INSTITUTION_KEY, false)
        }
    }

    /** Persists and updates the current token and basic user info. */
    fun saveToken(
        context: Context,
        value: String,
        nome: String? = null,
        email: String? = null,
        usuarioId: Long? = null,
        calendarLinked: Boolean = googleCalendarLinked,
        hasInstitution: Boolean = this.hasInstitution
    ) {
        token = value
        nomeUsuario = nome
        emailUsuario = email
        this.usuarioId = usuarioId
        googleCalendarLinked = calendarLinked
        this.hasInstitution = hasInstitution
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putString(USER_NAME_KEY, nomeUsuario)
            .putString(USER_EMAIL_KEY, emailUsuario)
            .putBoolean(CALENDAR_LINKED_KEY, googleCalendarLinked)
            .putBoolean(HAS_INSTITUTION_KEY, this.hasInstitution)
            .apply {
                if (usuarioId != null) {
                    putLong(USER_ID_KEY, usuarioId)
                } else {
                    remove(USER_ID_KEY)
                }
            }
            .apply()
    }

    /** Clears the persisted token, e.g. on logout. */
    fun clearToken(context: Context) {
        token = null
        nomeUsuario = null
        emailUsuario = null
        usuarioId = null
        googleCalendarLinked = false
        hasInstitution = false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(TOKEN_KEY)
            .remove(USER_NAME_KEY)
            .remove(USER_EMAIL_KEY)
            .remove(USER_ID_KEY)
            .remove(CALENDAR_LINKED_KEY)
            .remove(HAS_INSTITUTION_KEY)
            .apply()
    }

    fun updateCalendarLinkState(context: Context, linked: Boolean) {
        googleCalendarLinked = linked
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .apply()
    }

    fun updateHasInstitution(context: Context, hasInstitution: Boolean) {
        this.hasInstitution = hasInstitution
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(HAS_INSTITUTION_KEY, hasInstitution)
            .apply()
    }
}
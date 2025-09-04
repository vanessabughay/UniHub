package com.example.unihub.data.api

import android.content.Context

/**
 * Simple manager responsible for keeping the authentication token
 * and persisting it between app launches. The token is kept in memory
 * for quick access by the network layer and mirrored to
 * [SharedPreferences] so that screens opened after a process death still
 * have a valid token available.
 */
object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "auth_token"

    /** In‑memory representation of the token used by interceptors. */
    var token: String? = null
        private set

    /** Loads the token from shared preferences, if not already loaded. */
    fun loadToken(context: Context) {
        if (token == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            token = prefs.getString(TOKEN_KEY, null)
        }
    }

    /** Persists and updates the current token. */
    fun saveToken(context: Context, value: String) {
        token = value
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(TOKEN_KEY, value).apply()
    }

    /** Clears the persisted token, e.g. on logout. */
    fun clearToken(context: Context) {
        token = null
        var nomeUsuario: String? = null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(TOKEN_KEY).apply()
    }
}
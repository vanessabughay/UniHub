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
    private const val USER_NAME_KEY = "user_name"

    /** In‑memory representation of the token used by interceptors. */
    var token: String? = null
        private set

    var nomeUsuario: String? = null
        private set

    /** Loads the token from shared preferences, if not already loaded. */
    fun loadToken(context: Context) {
        if (token == null || nomeUsuario == null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            token = prefs.getString(TOKEN_KEY, null)
            nomeUsuario = prefs.getString(USER_NAME_KEY, null)
        }
    }

    /** Persists and updates the current token. */
    fun saveToken(context: Context, value: String, nome: String? = null) {
        token = value
        nomeUsuario = nome
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        nomeUsuario = nome
    }

    /** Clears the persisted token, e.g. on logout. */
    fun clearToken(context: Context) {
        token = null
        nomeUsuario = null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(TOKEN_KEY).remove(USER_NAME_KEY).apply()
    }
}
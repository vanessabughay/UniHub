package com.example.unihub.data.config

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_EMAIL_KEY = "user_email"
    private const val USER_ID_KEY = "user_id"
    private const val CALENDAR_LINKED_KEY = "google_calendar_linked"
    private const val HAS_INSTITUTION_KEY = "has_institution"
    private const val HAS_INSTITUTION_USER_PREFIX = "has_institution_user_"

    private fun userInstitutionKey(userId: Long) = "$HAS_INSTITUTION_USER_PREFIX$userId"

    private val applicationContextRef = AtomicReference<Context?>()

    /** In-memory representation of the token used by interceptors. */
    @Volatile
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
    @Synchronized
    fun loadToken(context: Context, forceReload: Boolean = false) {
        applicationContextRef.set(context.applicationContext)
        val shouldReload = forceReload || token.isNullOrBlank() || nomeUsuario == null ||
                emailUsuario == null || usuarioId == null

        if (!shouldReload) {
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        token = prefs.getString(TOKEN_KEY, null)?.takeIf { it.isNotBlank() }
        nomeUsuario = prefs.getString(USER_NAME_KEY, null)
        emailUsuario = prefs.getString(USER_EMAIL_KEY, null)
        usuarioId = prefs.takeIf { it.contains(USER_ID_KEY) }
            ?.getLong(USER_ID_KEY, -1L)
            ?.takeIf { id -> id > 0 }

        googleCalendarLinked = prefs.getBoolean(CALENDAR_LINKED_KEY, false)

        // Primeiro tenta por usuário, se não tiver, cai pro global antigo
        hasInstitution = usuarioId?.let { id ->
            prefs.getBoolean(userInstitutionKey(id), false)
        } ?: prefs.getBoolean(HAS_INSTITUTION_KEY, false)
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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        applicationContextRef.set(context.applicationContext)

        val persistedUserId = prefs.takeIf { it.contains(USER_ID_KEY) }
            ?.getLong(USER_ID_KEY, -1L)
            ?.takeIf { id -> id != -1L }

        val resolvedUserId = usuarioId?.takeIf { it > 0 }
            ?: this.usuarioId
            ?: persistedUserId

        // Recupera se esse usuário já tinha instituição salva
        val storedHasInstitution = resolvedUserId?.let { id ->
            prefs.getBoolean(userInstitutionKey(id), false)
        } ?: false

        val finalHasInstitution = hasInstitution || storedHasInstitution

        // Atualiza in-memory
        val normalizedToken = value.takeIf { it.isNotBlank() }

        token = normalizedToken
        nomeUsuario = nome
        emailUsuario = email
        this.usuarioId = resolvedUserId
        googleCalendarLinked = calendarLinked
        this.hasInstitution = finalHasInstitution

        // Persiste no SharedPreferences
        val editor = prefs.edit()
        if (normalizedToken != null) {
            editor.putString(TOKEN_KEY, normalizedToken)
        } else {
            editor.remove(TOKEN_KEY)
        }
        editor.putString(USER_NAME_KEY, nomeUsuario)
        editor.putString(USER_EMAIL_KEY, emailUsuario)
        editor.putBoolean(CALENDAR_LINKED_KEY, googleCalendarLinked)
        editor.putBoolean(HAS_INSTITUTION_KEY, finalHasInstitution)

        if (resolvedUserId != null) {
            editor.putLong(USER_ID_KEY, resolvedUserId)
            if (finalHasInstitution) {
                editor.putBoolean(userInstitutionKey(resolvedUserId), true)
            }
        } else {
            editor.remove(USER_ID_KEY)
        }

        editor.apply()
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
        val editor = prefs.edit()
        editor.remove(TOKEN_KEY)
        editor.remove(USER_NAME_KEY)
        editor.remove(USER_EMAIL_KEY)
        editor.remove(USER_ID_KEY)
        editor.remove(CALENDAR_LINKED_KEY)
        editor.remove(HAS_INSTITUTION_KEY)
        editor.apply()
    }

    fun initialize(context: Context) {
        applicationContextRef.set(context.applicationContext)
        loadToken(context.applicationContext, forceReload = true)
    }

    @Synchronized
    fun ensureTokenLoaded(): String? {
        if (!token.isNullOrBlank()) {
            return token
        }

        val ctx = applicationContextRef.get() ?: return token
        loadToken(ctx, forceReload = true)
        return token
    }

    fun requireToken(): String = ensureTokenLoaded()?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("Token de autenticação não encontrado")

    fun requireUsuarioId(): Long {
        ensureTokenLoaded()
        return usuarioId ?: throw IllegalStateException("ID do usuário não encontrado")
    }

    fun updateCalendarLinkState(context: Context, linked: Boolean) {
        googleCalendarLinked = linked
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(CALENDAR_LINKED_KEY, linked)
            .apply()
    }

    fun updateHasInstitution(context: Context, hasInstitution: Boolean) {
        this.hasInstitution = hasInstitution
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        editor.putBoolean(HAS_INSTITUTION_KEY, hasInstitution)

        // Também salva por usuário, se houver usuário
        val targetUserId =
            usuarioId
                ?: prefs.takeIf { it.contains(USER_ID_KEY) }
                    ?.getLong(USER_ID_KEY, -1L)
                    ?.takeIf { it != -1L }

        if (hasInstitution && targetUserId != null) {
            editor.putBoolean(userInstitutionKey(targetUserId), true)
        }

        editor.apply()
    }
}

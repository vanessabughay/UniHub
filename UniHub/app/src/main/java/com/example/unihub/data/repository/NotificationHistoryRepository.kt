package com.example.unihub.data.repository

import android.content.Context
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository responsável por armazenar o histórico de notificações locais do aplicativo.
 */
class NotificationHistoryRepository private constructor(context: Context) {

    data class NotificationEntry(
        val id: Long,
        val title: String,
        val message: String,
        val timestampMillis: Long,
        val shareInviteId: Long? = null,
        val shareActionPending: Boolean = false
    )

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val lock = Any()

    private val _historyFlow: MutableStateFlow<List<NotificationEntry>>

    private val lastId = AtomicLong(0)

    init {
        val storedEntries = loadEntries()
        _historyFlow = MutableStateFlow(storedEntries)

        val persistedLastId = preferences.getLong(KEY_LAST_ID, 0L)
        val currentMaxId = storedEntries.maxOfOrNull { it.id } ?: 0L
        lastId.set(max(persistedLastId, currentMaxId))
    }

    val historyFlow: StateFlow<List<NotificationEntry>> = _historyFlow.asStateFlow()

    fun logNotification(
        title: String,
        message: String,
        timestampMillis: Long,
        shareInviteId: Long? = null,
        shareActionPending: Boolean = false
    ) {
        synchronized(lock) {
            val existingEntry = shareInviteId?.let { id ->
                _historyFlow.value.firstOrNull { it.shareInviteId == id }
            }

            val entryId = existingEntry?.id ?: lastId.incrementAndGet()

            val effectiveTimestamp = if (existingEntry != null && shareInviteId != null) {
                existingEntry.timestampMillis
            } else {
                timestampMillis
            }

            val entry = NotificationEntry(
                id = entryId,
                title = title,
                message = message,
                timestampMillis = effectiveTimestamp,
                shareInviteId = shareInviteId,
                shareActionPending = if (shareInviteId != null) shareActionPending else false
            )

            val filteredHistory = _historyFlow.value.filterNot { it.id == entryId }
            val updatedHistory = (listOf(entry) + filteredHistory)
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory)
            preferences.edit().putLong(KEY_LAST_ID, lastId.get()).apply()
        }
    }

    fun markShareInviteHandled(inviteId: Long) {
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            val index = currentHistory.indexOfFirst { it.shareInviteId == inviteId }
            if (index == -1) return

            val entry = currentHistory[index]
            if (!entry.shareActionPending) return

            currentHistory[index] = entry.copy(shareActionPending = false)
            val updatedHistory = currentHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory)
        }
    }

    private fun loadEntries(): List<NotificationEntry> {
        val json = preferences.getString(KEY_ENTRIES, null) ?: return emptyList()

        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.optJSONObject(index) ?: continue
                    val id = obj.optLong(JSON_ID, -1L)
                    val title = obj.optString(JSON_TITLE, null)
                    val message = obj.optString(JSON_MESSAGE, null)
                    val timestamp = obj.optLong(JSON_TIMESTAMP, -1L)
                    val inviteId = if (obj.has(JSON_SHARE_INVITE_ID)) {
                        obj.optLong(JSON_SHARE_INVITE_ID, -1L).takeIf { it >= 0 }
                    } else {
                        null
                    }
                    val pending = if (obj.has(JSON_SHARE_ACTION_PENDING)) {
                        obj.optBoolean(JSON_SHARE_ACTION_PENDING, false)
                    } else {
                        false
                    }

                    if (id >= 0 && !title.isNullOrBlank() && !message.isNullOrBlank() && timestamp >= 0) {
                        add(
                            NotificationEntry(
                                id = id,
                                title = title,
                                message = message,
                                timestampMillis = timestamp,
                                shareInviteId = inviteId,
                                shareActionPending = pending
                            )
                        )
                    }
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun saveEntries(entries: List<NotificationEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put(JSON_ID, entry.id)
                put(JSON_TITLE, entry.title)
                put(JSON_MESSAGE, entry.message)
                put(JSON_TIMESTAMP, entry.timestampMillis)
                entry.shareInviteId?.let { inviteId ->
                    put(JSON_SHARE_INVITE_ID, inviteId)
                    put(JSON_SHARE_ACTION_PENDING, entry.shareActionPending)
                }
            }
            array.put(obj)
        }
        preferences.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "notification_history_repository"
        private const val KEY_ENTRIES = "notification_entries"
        private const val KEY_LAST_ID = "notification_last_id"
        private const val MAX_ENTRIES = 100

        private const val JSON_ID = "id"
        private const val JSON_TITLE = "title"
        private const val JSON_MESSAGE = "message"
        private const val JSON_TIMESTAMP = "timestamp"
        private const val JSON_SHARE_INVITE_ID = "share_invite_id"
        private const val JSON_SHARE_ACTION_PENDING = "share_action_pending"

        @Volatile
        private var instance: NotificationHistoryRepository? = null

        fun getInstance(context: Context): NotificationHistoryRepository {
            return instance ?: synchronized(this) {
                instance ?: NotificationHistoryRepository(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
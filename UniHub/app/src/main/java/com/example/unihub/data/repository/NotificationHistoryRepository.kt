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
        val timestampMillis: Long
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

    fun logNotification(title: String, message: String, timestampMillis: Long) {
        val entry = NotificationEntry(
            id = lastId.incrementAndGet(),
            title = title,
            message = message,
            timestampMillis = timestampMillis
        )

        synchronized(lock) {
            val updatedHistory = listOf(entry) + _historyFlow.value
            val limitedHistory = updatedHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = limitedHistory
            saveEntries(limitedHistory)
            preferences.edit().putLong(KEY_LAST_ID, lastId.get()).apply()
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

                    if (id >= 0 && !title.isNullOrBlank() && !message.isNullOrBlank() && timestamp >= 0) {
                        add(NotificationEntry(id, title, message, timestamp))
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
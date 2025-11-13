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
        val type: String? = null,
        val category: String? = null,
        val referenceId: Long? = null,
        val hasPendingInteraction: Boolean = false,
        val metadataJson: String? = null
    )

    private val preferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val lock = Any()

    private val _historyFlow: MutableStateFlow<List<NotificationEntry>>

    private val remoteLogger = NotificationRemoteLogger(context)

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
        type: String? = null,
        category: String? = null,
        referenceId: Long? = null,
        hasPendingInteraction: Boolean = false,
        metadata: Map<String, Any?>? = null,
        syncWithBackend: Boolean = true,
    ) {
        val normalizedType = type?.takeIf { it.isNotBlank() }
        val normalizedCategory = category?.takeIf { it.isNotBlank() }
        val metadataPayload = metadata?.takeIf { it.isNotEmpty() }

        synchronized(lock) {
            val existingEntry = when {
                referenceId != null && normalizedType != null -> {
                    _historyFlow.value.firstOrNull {
                        it.referenceId == referenceId && it.type == normalizedType
                    }
                }
                referenceId != null -> {
                    _historyFlow.value.firstOrNull { it.referenceId == referenceId }
                }
                else -> null
            }

            val entryId = existingEntry?.id ?: lastId.incrementAndGet()

            val effectiveTimestamp = if (existingEntry != null && referenceId != null) {
                existingEntry.timestampMillis
            } else {
                timestampMillis
            }

            val metadataJson = metadataPayload?.let { serializeMetadata(it) }
                ?: existingEntry?.metadataJson

            val resolvedReferenceId = referenceId ?: existingEntry?.referenceId
            val resolvedType = normalizedType ?: existingEntry?.type
            val resolvedCategory = normalizedCategory ?: existingEntry?.category
            val resolvedPending = when {
                resolvedReferenceId != null -> hasPendingInteraction
                existingEntry != null -> existingEntry.hasPendingInteraction
                else -> false
            }

            val entry = NotificationEntry(
                id = entryId,
                title = title,
                message = message,
                timestampMillis = effectiveTimestamp,
                type = resolvedType,
                category = resolvedCategory,
                referenceId = resolvedReferenceId,
                hasPendingInteraction = resolvedPending,
                metadataJson = metadataJson
            )

            val filteredHistory = _historyFlow.value.filterNot { it.id == entryId }
            val updatedHistory = (listOf(entry) + filteredHistory)
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory)
            preferences.edit().putLong(KEY_LAST_ID, lastId.get()).apply()
        }

        if (syncWithBackend) {
            remoteLogger.logNotification(
                title = title,
                message = message,
                timestampMillis = timestampMillis,
                type = normalizedType,
                category = normalizedCategory,
                referenceId = referenceId,
                hasPendingInteraction = hasPendingInteraction,
                metadata = metadataPayload
            )
        }

    }

    fun markShareInviteHandled(inviteId: Long) {
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            val index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == inviteId &&
                        (entry.type == SHARE_INVITE_TYPE || entry.category == SHARE_CATEGORY)
            }
            if (index == -1) return

            val entry = currentHistory[index]
            if (!entry.hasPendingInteraction) return

            currentHistory[index] = entry.copy(hasPendingInteraction = false)
            val updatedHistory = currentHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory)
        }
    }

    fun pruneShareNotifications(validReferences: Set<Long>) {
        pruneCategoryEntries(SHARE_CATEGORY, validReferences)
    }

    fun pruneContactNotifications(validReferences: Set<Long>) {
        pruneCategoryEntries(CONTACT_CATEGORY, validReferences)
    }

    private fun pruneCategoryEntries(category: String, validReferences: Set<Long>) {
        synchronized(lock) {
            val updatedHistory = _historyFlow.value.filter { entry ->
                val matchesCategory = entry.category?.equals(category, ignoreCase = true) == true
                val matchesType = when (category) {
                    SHARE_CATEGORY -> entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true
                            || entry.type?.equals(SHARE_INVITE_TYPE_RESPONSE, ignoreCase = true) == true
                    CONTACT_CATEGORY -> entry.type?.equals(CONTACT_INVITE_TYPE, ignoreCase = true) == true
                            || entry.type?.equals(CONTACT_INVITE_TYPE_RESPONSE, ignoreCase = true) == true
                    else -> false
                }

                if (matchesCategory || matchesType) {
                    val referenceId = entry.referenceId
                    referenceId != null && validReferences.contains(referenceId)
                } else {
                    true
                }
            }

            if (updatedHistory.size != _historyFlow.value.size) {
                _historyFlow.value = updatedHistory
                saveEntries(updatedHistory)
            }
        }
    }


    fun markContactInviteHandled(inviteId: Long) {
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            val index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == inviteId &&
                        (entry.type == CONTACT_INVITE_TYPE || entry.category == CONTACT_CATEGORY)
            }
            if (index == -1) return

            val entry = currentHistory[index]
            if (!entry.hasPendingInteraction) return

            currentHistory[index] = entry.copy(hasPendingInteraction = false)
            val updatedHistory = currentHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory)
        }
    }


    fun clear() {
        synchronized(lock) {
            _historyFlow.value = emptyList()
            preferences.edit()
                .remove(KEY_ENTRIES)
                .remove(KEY_LAST_ID)
                .apply()
            lastId.set(0)
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
                    val type = obj.optString(JSON_TYPE, null)?.takeIf { it.isNotBlank() }
                    val category = obj.optString(JSON_CATEGORY, null)?.takeIf { it.isNotBlank() }
                    val referenceId = when {
                        obj.has(JSON_REFERENCE_ID) && !obj.isNull(JSON_REFERENCE_ID) -> {
                            obj.optLong(JSON_REFERENCE_ID, Long.MIN_VALUE)
                                .takeIf { it != Long.MIN_VALUE }
                        }
                        obj.has(JSON_SHARE_INVITE_ID) && !obj.isNull(JSON_SHARE_INVITE_ID) -> {
                            obj.optLong(JSON_SHARE_INVITE_ID, -1L).takeIf { it >= 0 }
                        }
                        obj.has(JSON_CONTACT_INVITE_ID) && !obj.isNull(JSON_CONTACT_INVITE_ID) -> {
                            obj.optLong(JSON_CONTACT_INVITE_ID, -1L).takeIf { it >= 0 }
                        }
                        else -> null
                    }
                    val pending = when {
                        obj.has(JSON_PENDING_INTERACTION) -> obj.optBoolean(JSON_PENDING_INTERACTION, false)
                        obj.has(JSON_SHARE_ACTION_PENDING) -> obj.optBoolean(JSON_SHARE_ACTION_PENDING, false)
                        obj.has(JSON_CONTACT_ACTION_PENDING) -> obj.optBoolean(JSON_CONTACT_ACTION_PENDING, false)
                        else -> false
                    }
                    val metadataJson = obj.optString(JSON_METADATA, null)?.takeIf { it.isNotBlank() }

                    if (id >= 0 && !title.isNullOrBlank() && !message.isNullOrBlank() && timestamp >= 0) {
                        add(
                            NotificationEntry(
                                id = id,
                                title = title,
                                message = message,
                                timestampMillis = timestamp,
                                type = type,
                                category = category,
                                referenceId = referenceId,
                                hasPendingInteraction = pending,
                                metadataJson = metadataJson
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
                entry.type?.let { put(JSON_TYPE, it) }
                entry.category?.let { put(JSON_CATEGORY, it) }
                entry.referenceId?.let { reference ->
                    put(JSON_REFERENCE_ID, reference)
                    if (entry.type == SHARE_INVITE_TYPE || entry.category == SHARE_CATEGORY) {
                        put(JSON_SHARE_INVITE_ID, reference)
                        put(JSON_SHARE_ACTION_PENDING, entry.hasPendingInteraction)
                    }
                    if (entry.type == CONTACT_INVITE_TYPE || entry.category == CONTACT_CATEGORY) {
                        put(JSON_CONTACT_INVITE_ID, reference)
                        put(JSON_CONTACT_ACTION_PENDING, entry.hasPendingInteraction)
                    }
                }
                put(JSON_PENDING_INTERACTION, entry.hasPendingInteraction)
                entry.metadataJson?.let { put(JSON_METADATA, it) }
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
        private const val JSON_TYPE = "type"
        private const val JSON_CATEGORY = "category"
        private const val JSON_REFERENCE_ID = "reference_id"
        private const val JSON_SHARE_INVITE_ID = "share_invite_id"
        private const val JSON_SHARE_ACTION_PENDING = "share_action_pending"
        private const val JSON_CONTACT_INVITE_ID = "contact_invite_id"
        private const val JSON_CONTACT_ACTION_PENDING = "contact_action_pending"
        private const val JSON_PENDING_INTERACTION = "pending_interaction"
        private const val JSON_METADATA = "metadata"
        private const val SHARE_INVITE_TYPE_RESPONSE = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA"
        private const val SHARE_INVITE_TYPE = "DISCIPLINA_COMPARTILHAMENTO"
        private const val SHARE_CATEGORY = "COMPARTILHAMENTO"
        private const val CONTACT_INVITE_TYPE = "CONTATO_SOLICITACAO"
        private const val CONTACT_INVITE_TYPE_RESPONSE = "CONTATO_SOLICITACAO_RESPOSTA"
        private const val CONTACT_CATEGORY = "CONTATO"

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
    private fun serializeMetadata(metadata: Map<String, Any?>): String {
        val jsonObject = JSONObject()
        metadata.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        return jsonObject.toString()
    }
}
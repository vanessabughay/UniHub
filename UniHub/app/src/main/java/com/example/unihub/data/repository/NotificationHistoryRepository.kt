package com.example.unihub.data.repository

import android.content.Context
import com.example.unihub.R
import com.example.unihub.data.config.TokenManager
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
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

    private val appContext = context.applicationContext
    private val preferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val lock = Any()

    private val _historyFlow: MutableStateFlow<List<NotificationEntry>>

    private val remoteLogger = NotificationRemoteLogger()

    private var currentUserId: Long? = null

    private val lastId = AtomicLong(0)

    init {
        TokenManager.loadToken(appContext)
        currentUserId = TokenManager.usuarioId

        val storedEntries = loadEntries(currentUserId)
        _historyFlow = MutableStateFlow(storedEntries)

        val persistedLastId = preferences.getLong(lastIdKey(currentUserId), 0L)
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
    ): Boolean {
        val normalizedType = type?.takeIf { it.isNotBlank() }
        val normalizedCategory = category?.takeIf { it.isNotBlank() }
        val metadataPayload = metadata?.takeIf { it.isNotEmpty() }

        val isContactCategory = normalizedCategory?.equals(CONTACT_CATEGORY, ignoreCase = true) == true
        val isContactType = normalizedType?.equals(CONTACT_INVITE_TYPE, ignoreCase = true) == true ||
                normalizedType?.equals(CONTACT_INVITE_TYPE_RESPONSE, ignoreCase = true) == true

        val ehCategoriaFrequencia = normalizedCategory?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true
        val ehTipoFrequencia = normalizedType?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true ||
                normalizedType?.equals(FREQUENCIA_RESPOSTA_TYPE, ignoreCase = true) == true

        refreshUserContext()

        var shouldNotify = false

        synchronized(lock) {
            val activeUserId = currentUserId

            val existingEntry = when {
                referenceId != null && (normalizedType != null || normalizedCategory != null) -> {
                    _historyFlow.value.firstOrNull { entry ->
                        if (entry.referenceId != referenceId) return@firstOrNull false

                        val entryIsContact =
                            entry.category?.equals(CONTACT_CATEGORY, ignoreCase = true) == true ||
                                    entry.type?.equals(CONTACT_INVITE_TYPE, ignoreCase = true) == true ||
                                    entry.type?.equals(CONTACT_INVITE_TYPE_RESPONSE, ignoreCase = true) == true

                        if (ehCategoriaFrequencia || ehTipoFrequencia) {
                            val entradaEhFrequencia =
                                entry.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true ||
                                        entry.type?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true ||
                                        entry.type?.equals(FREQUENCIA_RESPOSTA_TYPE, ignoreCase = true) == true
                            return@firstOrNull entradaEhFrequencia || ehCategoriaFrequencia || ehTipoFrequencia
                        }

                        if (isContactCategory || isContactType || entryIsContact) {
                            entryIsContact || isContactCategory || isContactType
                        } else {
                            normalizedType != null && entry.type?.equals(normalizedType, ignoreCase = true) == true
                        }
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

            val metadataJson = if (metadataPayload != null) {
                val mergedMetadata = existingEntry?.metadataJson
                    ?.let { deserializeMetadata(it) }
                    ?: mutableMapOf()

                metadataPayload.forEach { (key, value) ->
                    if (value == null) {
                        mergedMetadata.remove(key)
                    } else {
                        mergedMetadata[key] = value
                    }
                }

                if (mergedMetadata.isEmpty()) {
                    null
                } else {
                    serializeMetadata(mergedMetadata)
                }
            } else {
                existingEntry?.metadataJson
            }

            val resolvedReferenceId = referenceId ?: existingEntry?.referenceId
            val resolvedType = normalizedType ?: existingEntry?.type
            val resolvedCategory = when {
                isContactCategory -> CONTACT_CATEGORY
                ehCategoriaFrequencia -> FREQUENCIA_CATEGORY
                existingEntry?.category?.equals(CONTACT_CATEGORY, ignoreCase = true) == true && isContactType -> CONTACT_CATEGORY
                existingEntry?.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true && ehTipoFrequencia -> FREQUENCIA_CATEGORY
                else -> normalizedCategory ?: existingEntry?.category
            }

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

            shouldNotify = existingEntry?.let { previous ->
                previous.title != entry.title ||
                        previous.message != entry.message ||
                        previous.timestampMillis != entry.timestampMillis ||
                        previous.type != entry.type ||
                        previous.category != entry.category ||
                        previous.referenceId != entry.referenceId ||
                        previous.hasPendingInteraction != entry.hasPendingInteraction ||
                        previous.metadataJson != entry.metadataJson
            } ?: true

            val filteredHistory = _historyFlow.value.filterNot { it.id == entryId }
            val updatedHistory = (listOf(entry) + filteredHistory)
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory, activeUserId)
            preferences.edit().putLong(lastIdKey(activeUserId), lastId.get()).apply()
        }

        if (syncWithBackend && shouldNotify) {
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

        return shouldNotify
    }

    /**
     * Remove entradas do histórico para a categoria e/ou tipos especificados. Útil para
     * sincronizar rapidamente o estado local quando o usuário desativa determinadas
     * notificações nas configurações.
     */
    fun clearByCategoryOrType(category: String? = null, types: Set<String> = emptySet()) {
        if (category == null && types.isEmpty()) return

        refreshUserContext()
        synchronized(lock) {
            val updatedHistory = _historyFlow.value.filterNot { entry ->
                val matchesCategory = category != null && entry.category?.equals(category, ignoreCase = true) == true
                val matchesType = entry.type?.let { currentType ->
                    types.any { type -> type.equals(currentType, ignoreCase = true) }
                } == true
                matchesCategory || matchesType
            }

            if (updatedHistory.size != _historyFlow.value.size) {
                _historyFlow.value = updatedHistory
                saveEntries(updatedHistory, currentUserId)
            }
        }
    }

    fun markShareInviteHandled(inviteId: Long) {
        refreshUserContext()
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            var index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == inviteId &&
                        (entry.type == SHARE_INVITE_TYPE || entry.category == SHARE_CATEGORY)
            }
            if (index == -1) {
                index = currentHistory.indexOfFirst { entry ->
                    entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true &&
                            extractShareInviteId(entry.metadataJson) == inviteId
                }
            }
            if (index == -1) return

            val entry = currentHistory[index]
            if (!entry.hasPendingInteraction) return

            val updatedEntry = entry.copy(hasPendingInteraction = false)
            currentHistory[index] = updatedEntry
            val updatedHistory = currentHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory, currentUserId)
        }
    }

    fun updateShareInviteResponse(
        referenceId: Long,
        accepted: Boolean,
        timestampMillis: Long = System.currentTimeMillis(),
        fallbackTitle: String? = null,
        fallbackMessage: String? = null
    ): Boolean {
        refreshUserContext()
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            var index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == referenceId && (
                        entry.category?.equals(SHARE_CATEGORY, ignoreCase = true) == true ||
                                entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true ||
                                entry.type?.equals(SHARE_INVITE_TYPE_RESPONSE, ignoreCase = true) == true
                        )
            }

            if (index == -1) {
                index = currentHistory.indexOfFirst { entry ->
                    entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true &&
                            extractShareInviteId(entry.metadataJson) == referenceId
                }
            }


            val existingEntry = currentHistory.getOrNull(index)
            val entryId = existingEntry?.id ?: lastId.incrementAndGet()

            val metadata = existingEntry?.metadataJson
                ?.let { deserializeMetadata(it) }
                ?: mutableMapOf()

            if (referenceId > 0) {
                metadata[KEY_SHARE_INVITE_ID] = referenceId
            } else {
                metadata.remove(KEY_SHARE_INVITE_ID)
            }


            var senderName = resolveShareSender(existingEntry, metadata)
            var disciplineName = resolveShareDiscipline(existingEntry, metadata)

            if (senderName == null || disciplineName == null) {
                val (fallbackSender, fallbackDiscipline) = guessShareInviteDetails(
                    existingEntry?.title ?: fallbackTitle,
                    existingEntry?.message ?: fallbackMessage
                )
                if (senderName == null) {
                    senderName = fallbackSender
                }
                if (disciplineName == null) {
                    disciplineName = fallbackDiscipline
                }
            }

            if (senderName != null) {
                metadata[KEY_SHARE_SENDER_NAME] = senderName
            } else {
                metadata.remove(KEY_SHARE_SENDER_NAME)
            }

            if (disciplineName != null) {
                metadata[KEY_SHARE_DISCIPLINE_NAME] = disciplineName
            } else {
                metadata.remove(KEY_SHARE_DISCIPLINE_NAME)
            }

            metadata[KEY_SHARE_LAST_ACTION] = if (accepted) {
                SHARE_ACTION_ACCEPTED
            } else {
                SHARE_ACTION_REJECTED
            }

            val resolvedTitle = existingEntry?.title
                ?: fallbackTitle
                ?: appContext.getString(R.string.share_notification_history_title)
            val resolvedSender = senderName
                ?: appContext.getString(R.string.share_notification_history_unknown_sender)
            val resolvedDiscipline = disciplineName
                ?: appContext.getString(R.string.share_notification_history_unknown_discipline)

            val message = if (accepted) {
                appContext.getString(
                    R.string.share_notification_history_accept_detail,
                    resolvedDiscipline,
                    resolvedSender
                )
            } else {
                appContext.getString(
                    R.string.share_notification_history_reject_detail,
                    resolvedDiscipline,
                    resolvedSender
                )
            }

            val metadataJson = metadata.takeIf { it.isNotEmpty() }
                ?.let { serializeMetadata(it) }

            val updatedEntry = NotificationEntry(
                id = entryId,
                title = resolvedTitle,
                message = message,
                timestampMillis = timestampMillis,
                type = existingEntry?.type ?: SHARE_INVITE_TYPE,
                category = existingEntry?.category ?: SHARE_CATEGORY,
                referenceId = referenceId,
                hasPendingInteraction = false,
                metadataJson = metadataJson
            )

            val shouldNotify = existingEntry?.let { previous ->
                previous.title != updatedEntry.title ||
                        previous.message != updatedEntry.message ||
                        previous.timestampMillis != updatedEntry.timestampMillis ||
                        previous.type != updatedEntry.type ||
                        previous.category != updatedEntry.category ||
                        previous.referenceId != updatedEntry.referenceId ||
                        previous.hasPendingInteraction != updatedEntry.hasPendingInteraction ||
                        previous.metadataJson != updatedEntry.metadataJson
            } ?: true

            if (index >= 0) {
                currentHistory[index] = updatedEntry
            } else {
                currentHistory.add(updatedEntry)
            }

            val cleanedHistory = currentHistory.filter { entry ->
                if (entry.id == updatedEntry.id) {
                    true
                } else if (referenceId > 0 && entry.type?.equals(SHARE_INVITE_TYPE, ignoreCase = true) == true) {
                    !entry.matchesShareInvite(referenceId)
                } else {
                    true
                }
            }

            val updatedHistory = cleanedHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory, currentUserId)

            return shouldNotify
        }
    }

    fun pruneShareNotifications(validReferences: Set<Long>) {
        refreshUserContext()
        pruneCategoryEntries(SHARE_CATEGORY, validReferences)
    }

    fun pruneContactNotifications(validReferences: Set<Long>) {
        refreshUserContext()
        pruneCategoryEntries(CONTACT_CATEGORY, validReferences)
    }

    fun shouldNotifyFrequencia(referenceId: Long): Boolean {
        refreshUserContext()
        return synchronized(lock) {
            val entry = _historyFlow.value.firstOrNull { current ->
                current.referenceId == referenceId && (
                        current.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true ||
                                current.type?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true
                        )
            }
            entry?.hasPendingInteraction != false
        }
    }



    fun logFrequenciaResponse(
        referenceId: Long,
        title: String,
        message: String,
        metadata: Map<String, Any?>? = null,
        syncWithBackend: Boolean = false,
    ) {
        val mergedMetadata = synchronized(lock) {
            val existingEntry = _historyFlow.value.firstOrNull { current ->
                current.referenceId == referenceId && (
                        current.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true ||
                                current.type?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true
                        )
            }

            val baseMetadata = existingEntry?.metadataJson
                ?.let { deserializeMetadata(it) }
                ?: mutableMapOf<String, Any?>()

            metadata?.forEach { (key, value) ->
                if (value == null) {
                    baseMetadata.remove(key)
                } else {
                    baseMetadata[key] = value
                }
            }

            baseMetadata.takeIf { it.isNotEmpty() }?.toMap()
        }
        logNotification(
            title = title,
            message = message,
            timestampMillis = System.currentTimeMillis(),
            type = FREQUENCIA_RESPOSTA_TYPE,
            category = FREQUENCIA_CATEGORY,
            referenceId = referenceId,
            hasPendingInteraction = false,
            metadata = mergedMetadata,
            syncWithBackend = syncWithBackend
        )
        markFrequenciaHandled(referenceId)
    }

    fun markFrequenciaHandled(referenceId: Long) {
        refreshUserContext()
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            val index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == referenceId && (
                        entry.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true ||
                                entry.type?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true
                        )
            }
            if (index == -1) return

            val entry = currentHistory[index]
            val updatedEntry = if (entry.hasPendingInteraction) {
                val resolvedEntry = entry.copy(hasPendingInteraction = false)
                currentHistory[index] = resolvedEntry
                val updatedHistory = currentHistory
                    .sortedByDescending { it.timestampMillis }
                    .take(MAX_ENTRIES)

                _historyFlow.value = updatedHistory
                saveEntries(updatedHistory, currentUserId)
                resolvedEntry
            } else {
                entry
            }

            cancelFrequenciaNotification(updatedEntry)
        }
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
                    val metadataReference = extractShareInviteId(entry.metadataJson)
                    val isValidReference = referenceId != null && validReferences.contains(referenceId)
                    val isValidMetadata = metadataReference != null && validReferences.contains(metadataReference)
                    isValidReference || isValidMetadata
                } else {
                    true
                }
            }

            if (updatedHistory.size != _historyFlow.value.size) {
                _historyFlow.value = updatedHistory
                saveEntries(updatedHistory, currentUserId)
            }
        }
    }

    private fun NotificationEntry.matchesShareInvite(referenceId: Long): Boolean {
        if (referenceId <= 0) return false
        if (this.referenceId == referenceId) return true
        return extractShareInviteId(metadataJson) == referenceId
    }

    private fun extractShareInviteId(metadataJson: String?): Long? {
        if (metadataJson.isNullOrBlank()) return null
        val metadata = deserializeMetadata(metadataJson)
        val rawValue = metadata[KEY_SHARE_INVITE_ID] ?: return null
        return parseShareInviteReference(rawValue)
    }

    private fun parseShareInviteReference(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull()
        else -> value?.toString()?.toLongOrNull()
    }


    fun markContactInviteHandled(inviteId: Long) {
        refreshUserContext()
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
            saveEntries(updatedHistory, currentUserId)
        }
    }

    private fun resolveShareSender(
        entry: NotificationEntry?,
        metadata: MutableMap<String, Any?>
    ): String? {
        fun sanitize(value: String?): String? {
            if (value.isNullOrBlank()) return null
            val cleaned = value.trim().trimEnd('.', ' ')
            return cleaned.takeIf { it.isNotEmpty() }
        }

        val metadataValue = sanitize(metadata[KEY_SHARE_SENDER_NAME]?.toString())
        if (metadataValue != null) {
            return metadataValue
        }

        val (sender, _) = NotificationHistoryRepository.guessShareInviteDetails(
            entry?.title,
            entry?.message
        )
        return sender
    }

    private fun resolveShareDiscipline(
        entry: NotificationEntry?,
        metadata: MutableMap<String, Any?>
    ): String? {
        fun sanitize(value: String?): String? {
            if (value.isNullOrBlank()) return null
            val cleaned = value.trim().trimEnd('.', ' ')
            return cleaned.takeIf { it.isNotEmpty() }
        }

        val metadataValue = sanitize(metadata[KEY_SHARE_DISCIPLINE_NAME]?.toString())
        if (metadataValue != null) {
            return metadataValue
        }

        val (_, discipline) = NotificationHistoryRepository.guessShareInviteDetails(
            entry?.title,
            entry?.message
        )
        return discipline
    }

    fun updateContactNotification(
        referenceId: Long,
        title: String,
        message: String,
        timestampMillis: Long,
        type: String
    ): Boolean {
        refreshUserContext()
        synchronized(lock) {
            val currentHistory = _historyFlow.value.toMutableList()
            val index = currentHistory.indexOfFirst { entry ->
                entry.referenceId == referenceId && (
                        entry.category?.equals(CONTACT_CATEGORY, ignoreCase = true) == true ||
                                entry.type?.equals(CONTACT_INVITE_TYPE, ignoreCase = true) == true ||
                                entry.type?.equals(CONTACT_INVITE_TYPE_RESPONSE, ignoreCase = true) == true
                        )
            }

            val existingEntry = currentHistory.getOrNull(index)
            val entryId = existingEntry?.id ?: lastId.incrementAndGet()
            val metadataJson = existingEntry?.metadataJson

            val updatedEntry = NotificationEntry(
                id = entryId,
                title = title,
                message = message,
                timestampMillis = timestampMillis,
                type = type,
                category = CONTACT_CATEGORY,
                referenceId = referenceId,
                hasPendingInteraction = false,
                metadataJson = metadataJson
            )
            val shouldNotify = existingEntry?.let { previous ->
                previous.title != updatedEntry.title ||
                        previous.message != updatedEntry.message ||
                        previous.timestampMillis != updatedEntry.timestampMillis ||
                        previous.type != updatedEntry.type
            } ?: true

            if (index >= 0) {
                currentHistory[index] = updatedEntry
            } else {
                currentHistory.add(updatedEntry)
            }

            val updatedHistory = currentHistory
                .sortedByDescending { it.timestampMillis }
                .take(MAX_ENTRIES)

            _historyFlow.value = updatedHistory
            saveEntries(updatedHistory, currentUserId)

            return shouldNotify
        }
        return false
    }

    fun clear() {
        refreshUserContext()
        synchronized(lock) {
            _historyFlow.value = emptyList()
            val activeUserId = currentUserId
            preferences.edit()
                .remove(entriesKey(activeUserId))
                .remove(lastIdKey(activeUserId))
                .apply()
            lastId.set(0)
        }
    }

    private fun loadEntries(userId: Long?): List<NotificationEntry> {
        val json = preferences.getString(entriesKey(userId), null) ?: return emptyList()

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

    private fun saveEntries(entries: List<NotificationEntry>, userId: Long?) {
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
                    if (entry.category == FREQUENCIA_CATEGORY || entry.type == FREQUENCIA_TYPE) {
                        put(JSON_PENDING_INTERACTION, entry.hasPendingInteraction)
                    }
                }
                put(JSON_PENDING_INTERACTION, entry.hasPendingInteraction)
                entry.metadataJson?.let { put(JSON_METADATA, it) }
            }
            array.put(obj)
        }
        preferences.edit().putString(entriesKey(userId), array.toString()).apply()
    }

    private fun refreshUserContext() {
        TokenManager.loadToken(appContext, forceReload = true)
        val resolvedUserId = TokenManager.usuarioId
        synchronized(lock) {
            if (resolvedUserId == currentUserId) {
                return
            }

            currentUserId = resolvedUserId
            val storedEntries = loadEntries(currentUserId)
            _historyFlow.value = storedEntries

            val persistedLastId = preferences.getLong(lastIdKey(currentUserId), 0L)
            val currentMaxId = storedEntries.maxOfOrNull { it.id } ?: 0L
            lastId.set(max(persistedLastId, currentMaxId))
        }
    }

    private fun entriesKey(userId: Long?): String {
        val suffix = userId?.toString() ?: KEY_SUFFIX_GUEST
        return "${KEY_ENTRIES}_$suffix"
    }

    private fun lastIdKey(userId: Long?): String {
        val suffix = userId?.toString() ?: KEY_SUFFIX_GUEST
        return "${KEY_LAST_ID}_$suffix"
    }

    companion object {
        private const val PREFS_NAME = "notification_history_repository"
        private const val KEY_ENTRIES = "notification_entries"
        private const val KEY_LAST_ID = "notification_last_id"
        private const val MAX_ENTRIES = 100
        private const val KEY_SUFFIX_GUEST = "guest"

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
        const val SHARE_INVITE_TYPE_RESPONSE = "DISCIPLINA_COMPARTILHAMENTO_RESPOSTA"
        const val SHARE_INVITE_TYPE = "DISCIPLINA_COMPARTILHAMENTO"
        const val KEY_SHARE_SENDER_NAME = "shareSenderName"
        const val KEY_SHARE_DISCIPLINE_NAME = "shareDisciplineName"
        const val KEY_SHARE_LAST_ACTION = "shareLastAction"
        const val KEY_SHARE_INVITE_ID = "shareInviteId"
        const val SHARE_ACTION_ACCEPTED = "ACCEPTED"
        const val SHARE_ACTION_REJECTED = "REJECTED"
        private const val SHARE_TITLE_INVITE_SEPARATOR = " compartilhou "
        private const val SHARE_TITLE_RESPONSE_SEPARATOR = " respondeu sobre "
        private const val SHARE_MESSAGE_INVITE_SEPARATOR = " quer compartilhar a disciplina "

        fun guessShareInviteDetails(title: String?, message: String?): Pair<String?, String?> {
            val senderFromTitle = extractBefore(title, SHARE_TITLE_INVITE_SEPARATOR)
                ?: extractBefore(title, SHARE_TITLE_RESPONSE_SEPARATOR)
            val disciplineFromTitle = extractAfter(title, SHARE_TITLE_INVITE_SEPARATOR)
                ?: extractAfter(title, SHARE_TITLE_RESPONSE_SEPARATOR)

            var sender = sanitizeShareText(senderFromTitle)
            var discipline = sanitizeShareText(disciplineFromTitle)

            if ((sender == null || discipline == null) && !message.isNullOrBlank()) {
                val index = message.indexOf(SHARE_MESSAGE_INVITE_SEPARATOR)
                if (index > 0) {
                    if (sender == null) {
                        sender = sanitizeShareText(message.substring(0, index))
                    }
                    if (discipline == null) {
                        discipline = sanitizeShareText(
                            message.substring(index + SHARE_MESSAGE_INVITE_SEPARATOR.length)
                        )
                    }
                }
            }

            return sender to discipline
        }

        const val SHARE_CATEGORY = "COMPARTILHAMENTO"
        const val CONTACT_INVITE_TYPE = "CONTATO_SOLICITACAO"
        const val CONTACT_INVITE_TYPE_RESPONSE = "CONTATO_SOLICITACAO_RESPOSTA"
        const val CONTACT_CATEGORY = "CONTATO"
        const val GROUP_MEMBER_CATEGORY = "GRUPO_MEMBRO_ADICIONADO"
        const val TAREFA_ATRIBUICAO_CATEGORY = "TAREFA_ATRIBUIDA"
        const val TAREFA_COMENTARIO_CATEGORY = "TAREFA_COMENTARIO"
        const val TAREFA_PRAZO_TYPE = "TAREFA_PRAZO"
        const val TAREFA_CATEGORY = "TAREFA"
        const val AVALIACAO_LEMBRETE_TYPE = "AVALIACAO_LEMBRETE"
        const val AVALIACAO_CATEGORY = "AVALIACAO"
        const val FREQUENCIA_TYPE = "PRESENCA_AULA"
        const val FREQUENCIA_RESPOSTA_TYPE = "PRESENCA_AULA_RESPOSTA"
        const val FREQUENCIA_CATEGORY = "PRESENCA"

        @Volatile
        private var instance: NotificationHistoryRepository? = null

        fun getInstance(context: Context): NotificationHistoryRepository {
            val existing = instance
            if (existing != null) {
                existing.refreshUserContext()
                return existing
            }

            return synchronized(this) {
                val current = instance
                if (current != null) {
                    current.refreshUserContext()
                    current
                } else {
                    val created = NotificationHistoryRepository(context.applicationContext)
                    instance = created
                    created
                }
            }
        }

        fun buildFrequenciaReferenceId(disciplinaId: Long, occurrenceEpochDay: Long): Long {
            val upper = disciplinaId and 0xFFFFFFFFL
            val lower = occurrenceEpochDay and 0xFFFFFFFFL
            return (upper shl 32) or (lower and 0xFFFFFFFFL)
        }

        private fun extractBefore(text: String?, separator: String): String? {
            if (text.isNullOrBlank()) return null
            val index = text.indexOf(separator)
            if (index <= 0) return null
            return text.substring(0, index)
        }

        private fun extractAfter(text: String?, separator: String): String? {
            if (text.isNullOrBlank()) return null
            val index = text.indexOf(separator)
            if (index < 0) return null
            return text.substring(index + separator.length)
        }

        private fun sanitizeShareText(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val cleaned = raw.trim().trimEnd('.', ' ')
            return cleaned.takeIf { it.isNotEmpty() }
        }
    }
    private fun serializeMetadata(metadata: Map<String, Any?>): String {
        val jsonObject = JSONObject()
        metadata.forEach { (key, value) ->
            if (value == null) {
                jsonObject.put(key, JSONObject.NULL)
            } else {
                jsonObject.put(key, value)
            }
        }
        return jsonObject.toString()
    }

    private fun deserializeMetadata(json: String): MutableMap<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        runCatching {
            val jsonObject = JSONObject(json)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.opt(key)
                result[key] = if (value == JSONObject.NULL) null else value
            }
        }
        return result
    }

    private fun cancelFrequenciaNotification(entry: NotificationEntry) {
        val entradaEhFrequencia = entry.category?.equals(FREQUENCIA_CATEGORY, ignoreCase = true) == true ||
                entry.type?.equals(FREQUENCIA_TYPE, ignoreCase = true) == true
        if (!entradaEhFrequencia) {
            return
        }

        val metadata = entry.metadataJson ?: return
        runCatching {
            val json = JSONObject(metadata)

            // Primeiro, tenta usar notificationId direto do metadata
            val notificationId = when {
                json.has("notificationId") && !json.isNull("notificationId") -> {
                    when (val value = json.opt("notificationId")) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull()
                        else -> null
                    }
                }
                else -> null
            }

            if (notificationId != null) {
                NotificationManagerCompat.from(appContext).cancel(notificationId)
                return@runCatching
            }

            // Se não tiver notificationId, tenta reconstruir a partir de disciplinaId + inicio
            val disciplinaId = when {
                json.has("disciplinaId") && !json.isNull("disciplinaId") -> {
                    json.optLong("disciplinaId", Long.MIN_VALUE)
                        .takeIf { it != Long.MIN_VALUE }
                }
                else -> null
            } ?: return

            val inicio = when {
                json.has("inicio") && !json.isNull("inicio") -> {
                    when (val value = json.opt("inicio")) {
                        is Number -> value.toInt()
                        is String -> value.toIntOrNull()
                        else -> null
                    }
                }
                else -> null
            } ?: return

            val fallbackNotificationId =
                abs((disciplinaId.toString() + inicio.toString()).hashCode())

            NotificationManagerCompat.from(appContext).cancel(fallbackNotificationId)
        }
    }

}
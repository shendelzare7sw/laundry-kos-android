package com.laundry_kos.core.notifikasi

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "laundry_kos_notifications",
)

@Serializable
data class StoredNotification(
    val id: String,
    val propertiId: Int,
    val type: String, // "ACTIVITY", "DANGER", "WARN", "INFO"
    val title: String,
    val subtitle: String,
    val timestampMs: Long,
    val penghuniId: Int? = null,
    val laundryId: Int? = null,
)

@Serializable
data class LaundrySnapshotItem(
    val label: String,
    val tanggal: String,
    val fingerprint: String,
)

@Serializable
data class PenghuniSnapshotItem(
    val nama: String,
    val status: String,
    val kamarNomor: String?,
    val fingerprint: String,
)

@Serializable
data class PropertyActivitySnapshot(
    val initialized: Boolean = false,
    val laundry: Map<Int, LaundrySnapshotItem> = emptyMap(),
    val penghuni: Map<Int, PenghuniSnapshotItem> = emptyMap(),
    val quotaStatus: Map<Int, String> = emptyMap(),
)

@Serializable
data class NotificationStoreState(
    val history: List<StoredNotification> = emptyList(),
    val readIds: Set<String> = emptySet(),
    val dismissedIds: Set<String> = emptySet(),
    val snapshots: Map<Int, PropertyActivitySnapshot> = emptyMap(),
)

class NotificationStore(private val context: Context) {
    private val dataStore = context.applicationContext.notificationDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val _state = MutableStateFlow(NotificationStoreState())
    val state: StateFlow<NotificationStoreState> = _state.asStateFlow()

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                _state.value = decode(prefs[KEY_PAYLOAD])
            }
        }
    }

    suspend fun current(): NotificationStoreState {
        val prefs = dataStore.data.first()
        val parsed = decode(prefs[KEY_PAYLOAD])
        _state.value = parsed
        return parsed
    }

    suspend fun recordSyncResult(
        propertiId: Int,
        newSnapshot: PropertyActivitySnapshot,
        newEvents: List<StoredNotification>,
    ) {
        val existing = _state.value
        if (newEvents.isEmpty() && existing.snapshots[propertiId] == newSnapshot) {
            return
        }
        updateState { cur ->
            if (newEvents.isEmpty() && cur.snapshots[propertiId] == newSnapshot) {
                return@updateState cur
            }
            val mergedHistory = (newEvents + cur.history)
                .distinctBy { it.id }
                .sortedByDescending { it.timestampMs }
                .take(MAX_HISTORY)
            cur.copy(
                history = mergedHistory,
                snapshots = cur.snapshots + (propertiId to newSnapshot),
            )
        }
    }

    suspend fun markAsRead(id: String) {
        updateState { cur ->
            cur.copy(readIds = cur.readIds + id)
        }
    }

    suspend fun markAllAsRead(allVisibleIds: Collection<String>) {
        if (allVisibleIds.isEmpty()) return
        updateState { cur ->
            cur.copy(readIds = cur.readIds + allVisibleIds)
        }
    }

    suspend fun clearAll(propertiId: Int?, allVisibleIds: Collection<String>) {
        updateState { cur ->
            val remainingHistory = if (propertiId == null) {
                emptyList()
            } else {
                cur.history.filterNot { it.propertiId == propertiId }
            }
            cur.copy(
                history = remainingHistory,
                readIds = cur.readIds + allVisibleIds,
                dismissedIds = cur.dismissedIds + allVisibleIds,
            )
        }
        SystemNotificationHelper.cancelAll(context)
    }

    suspend fun resetAll() {
        dataStore.edit { it.remove(KEY_PAYLOAD) }
        _state.value = NotificationStoreState()
        SystemNotificationHelper.cancelAll(context)
    }

    private suspend fun updateState(transform: (NotificationStoreState) -> NotificationStoreState) {
        dataStore.edit { prefs ->
            val current = decode(prefs[KEY_PAYLOAD])
            val updated = transform(current)
            if (updated == current) return@edit
            prefs[KEY_PAYLOAD] = json.encodeToString(updated)
            _state.value = updated
        }
    }

    private fun decode(raw: String?): NotificationStoreState {
        if (raw.isNullOrBlank()) return NotificationStoreState()
        return runCatching { json.decodeFromString<NotificationStoreState>(raw) }
            .getOrDefault(NotificationStoreState())
    }

    companion object {
        private val KEY_PAYLOAD = stringPreferencesKey("notification_store_v1")
        private const val MAX_HISTORY = 60
    }
}

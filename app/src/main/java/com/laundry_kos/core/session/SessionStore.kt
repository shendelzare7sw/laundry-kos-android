package com.laundry_kos.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.laundry_kos.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "laundry_kos_session",
)

data class StoredSession(
    val token: String? = null,
    val propertiId: Int? = null,
    val baseUrl: String = BuildConfig.API_BASE_URL,
    val loaded: Boolean = false,
)

class SessionStore(context: Context) {
    private val dataStore = context.applicationContext.sessionDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(StoredSession())
    val state: StateFlow<StoredSession> = _state.asStateFlow()

    val currentToken: String?
        get() = _state.value.token

    val currentPropertiId: Int?
        get() = _state.value.propertiId

    val currentBaseUrl: String
        get() = _state.value.baseUrl

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                val savedUrl = if (BuildConfig.DEBUG) {
                    prefs[KEY_DEBUG_BASE_URL]?.takeIf { it.isNotBlank() } ?: BuildConfig.API_BASE_URL
                } else {
                    BuildConfig.API_BASE_URL
                }
                _state.value = StoredSession(
                    token = prefs[KEY_TOKEN]?.takeIf { it.isNotBlank() },
                    propertiId = prefs[KEY_PROPERTI_ID],
                    baseUrl = normalizeBaseUrl(savedUrl),
                    loaded = true,
                )
            }
        }
    }

    suspend fun awaitLoaded(): StoredSession {
        if (_state.value.loaded) return _state.value
        val prefs = dataStore.data.first()
        val savedUrl = if (BuildConfig.DEBUG) {
            prefs[KEY_DEBUG_BASE_URL]?.takeIf { it.isNotBlank() } ?: BuildConfig.API_BASE_URL
        } else {
            BuildConfig.API_BASE_URL
        }
        val loaded = StoredSession(
            token = prefs[KEY_TOKEN]?.takeIf { it.isNotBlank() },
            propertiId = prefs[KEY_PROPERTI_ID],
            baseUrl = normalizeBaseUrl(savedUrl),
            loaded = true,
        )
        _state.value = loaded
        return loaded
    }

    suspend fun saveSession(token: String, propertiId: Int?) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            if (propertiId != null) {
                prefs[KEY_PROPERTI_ID] = propertiId
            } else {
                prefs.remove(KEY_PROPERTI_ID)
            }
        }
    }

    suspend fun savePropertiId(propertiId: Int?) {
        dataStore.edit { prefs ->
            if (propertiId != null) {
                prefs[KEY_PROPERTI_ID] = propertiId
            } else {
                prefs.remove(KEY_PROPERTI_ID)
            }
        }
    }

    suspend fun saveDebugBaseUrl(url: String) {
        if (!BuildConfig.DEBUG) return
        val normalized = normalizeBaseUrl(url)
        dataStore.edit { prefs ->
            prefs[KEY_DEBUG_BASE_URL] = normalized
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_PROPERTI_ID)
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("sanctum_token")
        private val KEY_PROPERTI_ID = intPreferencesKey("active_properti_id")
        private val KEY_DEBUG_BASE_URL = stringPreferencesKey("debug_api_base_url")

        fun normalizeBaseUrl(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return BuildConfig.API_BASE_URL
            val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                trimmed
            } else {
                "http://$trimmed"
            }
            return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        }
    }
}

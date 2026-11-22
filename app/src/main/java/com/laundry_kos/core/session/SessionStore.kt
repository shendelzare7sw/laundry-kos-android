package com.laundry_kos.core.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "laundry_kos_session",
)

data class StoredSession(
    val token: String? = null,
    val propertiId: Int? = null,
    val userId: Int? = null,
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

    val currentUserId: Int?
        get() = _state.value.userId

    init {
        scope.launch {
            dataStore.data.collect { prefs ->
                _state.value = StoredSession(
                    token = prefs[KEY_TOKEN]?.takeIf { it.isNotBlank() },
                    propertiId = prefs[KEY_PROPERTI_ID],
                    userId = prefs[KEY_USER_ID],
                    loaded = true,
                )
            }
        }
    }

    suspend fun awaitLoaded(): StoredSession {
        if (_state.value.loaded) return _state.value
        val prefs = dataStore.data.first()
        val loaded = StoredSession(
            token = prefs[KEY_TOKEN]?.takeIf { it.isNotBlank() },
            propertiId = prefs[KEY_PROPERTI_ID],
            userId = prefs[KEY_USER_ID],
            loaded = true,
        )
        _state.value = loaded
        return loaded
    }

    suspend fun saveSession(token: String, propertiId: Int?, userId: Int? = null) {
        dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            if (propertiId != null) {
                prefs[KEY_PROPERTI_ID] = propertiId
            } else {
                prefs.remove(KEY_PROPERTI_ID)
            }
            if (userId != null) {
                prefs[KEY_USER_ID] = userId
            }
        }
    }

    suspend fun saveUserId(userId: Int?) {
        dataStore.edit { prefs ->
            if (userId != null) {
                prefs[KEY_USER_ID] = userId
            } else {
                prefs.remove(KEY_USER_ID)
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

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_PROPERTI_ID)
            prefs.remove(KEY_USER_ID)
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("sanctum_token")
        private val KEY_PROPERTI_ID = intPreferencesKey("active_properti_id")
        private val KEY_USER_ID = intPreferencesKey("current_user_id")
    }
}

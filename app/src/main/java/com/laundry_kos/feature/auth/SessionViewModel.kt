package com.laundry_kos.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.di.AppContainer
import com.laundry_kos.core.network.ApiError
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.network.GlobalApiEvent
import com.laundry_kos.core.notifikasi.ActivitySyncWorker
import com.laundry_kos.core.notifikasi.NotificationStore
import com.laundry_kos.core.notifikasi.SystemNotificationHelper
import com.laundry_kos.data.dto.PermissionsDto
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.data.dto.SimpanPropertiRequestDto
import com.laundry_kos.data.dto.UserDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SessionUiState {
    data object Checking : SessionUiState

    data class Unauthenticated(
        val bannerTitle: String? = null,
        val bannerMessage: String? = null,
        val bannerIsError: Boolean = true,
        val isLoggingIn: Boolean = false,
        val loginError: ApiError? = null,
    ) : SessionUiState

    data class SelectProperti(
        val user: UserDto,
        val propertiList: List<PropertiDto>,
        val message: String? = null,
        val dialogError: String? = null,
        val snackbarMessage: String? = null,
        val isSubmitting: Boolean = false,
        val isRefreshing: Boolean = false,
    ) : SessionUiState

    data class Authenticated(
        val user: UserDto,
        val propertiAktif: PropertiDto,
        val propertiList: List<PropertiDto>,
        val permissions: PermissionsDto,
    ) : SessionUiState
}

class SessionViewModel(
    private val repository: LaundryKosRepository,
    private val appContext: Context,
    private val notificationStore: NotificationStore,
) : ViewModel() {

    private val _state = MutableStateFlow<SessionUiState>(SessionUiState.Checking)
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    init {
        observeGlobalEvents()
        bootstrap()
    }

    private suspend fun cleanupSessionSideEffects() {
        ActivitySyncWorker.cancel(appContext)
        SystemNotificationHelper.cancelAll(appContext)
        notificationStore.resetAll()
    }

    private fun isGlobalRole(user: UserDto): Boolean =
        user.role == "owner" || user.role == "dev_admin"

    private fun titleForBlockedCode(code: String?): String = when (code) {
        "AKUN_NONAKTIF" -> "Akun Dinonaktifkan"
        "PROPERTI_TIDAK_AKTIF", "PROPERTI_TIDAK_TERIKAT" -> "Properti Tidak Aktif"
        "UNAUTHORIZED" -> "Sesi Login Berakhir"
        else -> "Akses Ditolak"
    }

    private fun observeGlobalEvents() {
        viewModelScope.launch {
            repository.eventBus.events.collect { event ->
                when (event) {
                    is GlobalApiEvent.SessionRevoked -> {
                        repository.sessionStore.clearSession()
                        cleanupSessionSideEffects()
                        _state.value = SessionUiState.Unauthenticated(
                            bannerTitle = titleForBlockedCode(event.code),
                            bannerMessage = event.message,
                            bannerIsError = true,
                        )
                    }
                    is GlobalApiEvent.Unauthorized -> {
                        repository.sessionStore.clearSession()
                        cleanupSessionSideEffects()
                        _state.value = SessionUiState.Unauthenticated(
                            bannerTitle = "Sesi Login Berakhir",
                            bannerMessage = event.message,
                            bannerIsError = true,
                        )
                    }
                    is GlobalApiEvent.PropertiRequired -> {
                        val currentUser = when (val s = _state.value) {
                            is SessionUiState.Authenticated -> s.user
                            is SessionUiState.SelectProperti -> s.user
                            else -> null
                        }
                        if (currentUser != null) {
                            repository.sessionStore.savePropertiId(null)
                            _state.value = SessionUiState.SelectProperti(
                                user = currentUser,
                                propertiList = event.propertiList,
                                message = event.message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun bootstrap() {
        viewModelScope.launch {
            _state.value = SessionUiState.Checking
            val saved = repository.sessionStore.awaitLoaded()
            if (saved.token.isNullOrBlank()) {
                _state.value = SessionUiState.Unauthenticated()
                return@launch
            }

            when (val res = repository.fetchMe()) {
                is ApiResult.Success -> {
                    val user = res.data.user
                    ActivitySyncWorker.schedule(appContext)
                    // Sesuai workflow web (AuthenticatedSessionController & PastikanPropertiAktif):
                    // Owner / DevAdmin tanpa properti terpilih di sesi tetap mendarat di layar Pilih Gedung.
                    if (isGlobalRole(user) && saved.propertiId == null) {
                        repository.sessionStore.savePropertiId(null)
                        _state.value = SessionUiState.SelectProperti(
                            user = user,
                            propertiList = res.data.propertiList,
                        )
                    } else {
                        applyAuthenticatedOrPicker(
                            user = user,
                            propertiAktif = res.data.propertiAktif,
                            propertiList = res.data.propertiList,
                            permissions = res.data.permissions,
                        )
                    }
                }
                is ApiResult.Failure -> {
                    if (res.error.isUnauthorized || res.error.isForbiddenBlocked) {
                        repository.sessionStore.clearSession()
                        cleanupSessionSideEffects()
                    }
                    _state.value = SessionUiState.Unauthenticated(
                        bannerTitle = if (res.error.isAccountOrPropertyBlocked || res.error.isUnauthorized) {
                            titleForBlockedCode(res.error.code ?: if (res.error.isUnauthorized) "UNAUTHORIZED" else null)
                        } else {
                            null
                        },
                        bannerMessage = res.error.message,
                        bannerIsError = true,
                    )
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = SessionUiState.Unauthenticated(isLoggingIn = true)
            when (val res = repository.login(username, password)) {
                is ApiResult.Success -> {
                    val user = res.data.user
                    ActivitySyncWorker.schedule(appContext)
                    // Persis AuthenticatedSessionController::store di Laravel Web:
                    // Owner & DevAdmin SELALU diarahkan ke Pilih Gedung dahulu setelah login!
                    if (isGlobalRole(user)) {
                        repository.sessionStore.savePropertiId(null)
                        _state.value = SessionUiState.SelectProperti(
                            user = user,
                            propertiList = res.data.propertiList,
                        )
                    } else {
                        applyAuthenticatedOrPicker(
                            user = user,
                            propertiAktif = res.data.propertiAktif,
                            propertiList = res.data.propertiList,
                            permissions = res.data.permissions,
                        )
                    }
                }
                is ApiResult.Failure -> {
                    _state.value = SessionUiState.Unauthenticated(
                        isLoggingIn = false,
                        loginError = res.error,
                    )
                }
            }
        }
    }

    fun refreshPropertiList() {
        if (_state.value !is SessionUiState.SelectProperti) return
        viewModelScope.launch {
            _state.update { s ->
                if (s is SessionUiState.SelectProperti) s.copy(isRefreshing = true, message = null) else s
            }
            when (val res = repository.fetchProperti()) {
                is ApiResult.Success -> _state.update { s ->
                    if (s is SessionUiState.SelectProperti) {
                        s.copy(propertiList = res.data.data, isRefreshing = false, message = null)
                    } else {
                        s
                    }
                }
                is ApiResult.Failure -> _state.update { s ->
                    if (s is SessionUiState.SelectProperti) {
                        s.copy(isRefreshing = false, message = res.error.summaryMessage())
                    } else {
                        s
                    }
                }
            }
        }
    }

    fun clearPropertiDialogError() {
        _state.update { s ->
            if (s is SessionUiState.SelectProperti) s.copy(dialogError = null) else s
        }
    }

    fun tambahProperti(
        request: SimpanPropertiRequestDto,
        onCreated: (PropertiDto) -> Unit,
    ) {
        if (_state.value !is SessionUiState.SelectProperti) return
        viewModelScope.launch {
            _state.update { s ->
                if (s is SessionUiState.SelectProperti) s.copy(isSubmitting = true, message = null, dialogError = null) else s
            }
            when (val res = repository.tambahProperti(request)) {
                is ApiResult.Success -> {
                    _state.update { s ->
                        if (s is SessionUiState.SelectProperti) {
                            s.copy(isSubmitting = false, dialogError = null, snackbarMessage = res.data.message)
                        } else {
                            s
                        }
                    }
                    refreshPropertiList()
                    onCreated(res.data.data)
                }
                is ApiResult.Failure -> _state.update { s ->
                    if (s is SessionUiState.SelectProperti) {
                        s.copy(isSubmitting = false, dialogError = res.error.summaryMessage())
                    } else {
                        s
                    }
                }
            }
        }
    }

    fun ubahProperti(
        propertiId: Int,
        request: SimpanPropertiRequestDto,
        onUpdated: () -> Unit,
    ) {
        if (_state.value !is SessionUiState.SelectProperti) return
        viewModelScope.launch {
            _state.update { s ->
                if (s is SessionUiState.SelectProperti) s.copy(isSubmitting = true, message = null, dialogError = null) else s
            }
            when (val res = repository.ubahProperti(propertiId, request)) {
                is ApiResult.Success -> {
                    _state.update { s ->
                        if (s is SessionUiState.SelectProperti) {
                            s.copy(isSubmitting = false, dialogError = null, snackbarMessage = res.data.message)
                        } else {
                            s
                        }
                    }
                    onUpdated()
                    refreshPropertiList()
                }
                is ApiResult.Failure -> _state.update { s ->
                    if (s is SessionUiState.SelectProperti) {
                        s.copy(isSubmitting = false, dialogError = res.error.summaryMessage())
                    } else {
                        s
                    }
                }
            }
        }
    }

    fun clearSelectPropertiSnackbar() {
        _state.update { s ->
            if (s is SessionUiState.SelectProperti) s.copy(snackbarMessage = null) else s
        }
    }

    fun selectProperti(propertiId: Int) {
        viewModelScope.launch {
            val current = _state.value
            if (current is SessionUiState.SelectProperti) {
                _state.value = current.copy(isSubmitting = true, message = null)
            }

            when (val res = repository.selectProperti(propertiId)) {
                is ApiResult.Success -> applyAuthenticatedOrPicker(
                    user = res.data.user,
                    propertiAktif = res.data.propertiAktif,
                    propertiList = res.data.propertiList,
                    permissions = res.data.permissions,
                )
                is ApiResult.Failure -> {
                    if (current is SessionUiState.SelectProperti && !res.error.isAccountOrPropertyBlocked && !res.error.isUnauthorized) {
                        _state.value = current.copy(isSubmitting = false, message = res.error.message)
                    }
                }
            }
        }
    }

    /**
     * Persis `POST /properti/lepas` di Laravel Web: lepas konteks properti aktif
     * dan kembalikan Owner/DevAdmin ke halaman Pilih Gedung.
     */
    fun releaseProperti() {
        val current = _state.value as? SessionUiState.Authenticated ?: return
        if (!isGlobalRole(current.user)) return
        viewModelScope.launch {
            repository.sessionStore.savePropertiId(null)
            _state.value = SessionUiState.SelectProperti(
                user = current.user,
                propertiList = current.propertiList,
            )
            refreshPropertiList()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            cleanupSessionSideEffects()
            _state.value = SessionUiState.Unauthenticated(
                bannerMessage = "Anda telah keluar dari akun.",
                bannerIsError = false,
            )
        }
    }

    private fun applyAuthenticatedOrPicker(
        user: UserDto,
        propertiAktif: PropertiDto?,
        propertiList: List<PropertiDto>,
        permissions: PermissionsDto,
    ) {
        if (propertiAktif == null) {
            _state.value = SessionUiState.SelectProperti(
                user = user,
                propertiList = propertiList,
                message = if (propertiList.isEmpty()) {
                    "Belum ada properti aktif yang dapat diakses akun ini."
                } else {
                    "Silakan pilih gedung/properti terlebih dahulu."
                },
            )
        } else {
            ActivitySyncWorker.schedule(appContext)
            _state.value = SessionUiState.Authenticated(
                user = user,
                propertiAktif = propertiAktif,
                propertiList = propertiList,
                permissions = permissions,
            )
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(
                repository = container.repository,
                appContext = container.appContext,
                notificationStore = container.notificationStore,
            ) as T
    }
}

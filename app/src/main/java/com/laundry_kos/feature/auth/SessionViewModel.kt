package com.laundry_kos.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiError
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.network.GlobalApiEvent
import com.laundry_kos.data.dto.PermissionsDto
import com.laundry_kos.data.dto.PropertiDto
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
        val bannerMessage: String? = null,
        val isLoggingIn: Boolean = false,
        val loginError: ApiError? = null,
    ) : SessionUiState

    data class SelectProperti(
        val user: UserDto,
        val propertiList: List<PropertiDto>,
        val message: String? = null,
        val isSubmitting: Boolean = false,
    ) : SessionUiState

    data class Authenticated(
        val user: UserDto,
        val propertiAktif: PropertiDto,
        val propertiList: List<PropertiDto>,
        val permissions: PermissionsDto,
        val showPropertiSwitcher: Boolean = false,
        val isSwitchingProperti: Boolean = false,
    ) : SessionUiState
}

class SessionViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SessionUiState>(SessionUiState.Checking)
    val state: StateFlow<SessionUiState> = _state.asStateFlow()

    val storedSession = repository.sessionStore.state

    init {
        observeGlobalEvents()
        bootstrap()
    }

    private fun observeGlobalEvents() {
        viewModelScope.launch {
            repository.eventBus.events.collect { event ->
                when (event) {
                    is GlobalApiEvent.Unauthorized -> {
                        repository.sessionStore.clearSession()
                        _state.value = SessionUiState.Unauthenticated(bannerMessage = event.message)
                    }
                    is GlobalApiEvent.PropertiRequired -> {
                        val currentUser = when (val s = _state.value) {
                            is SessionUiState.Authenticated -> s.user
                            is SessionUiState.SelectProperti -> s.user
                            else -> null
                        }
                        if (currentUser != null) {
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
                is ApiResult.Success -> applyMeResult(
                    user = res.data.user,
                    propertiAktif = res.data.propertiAktif,
                    propertiList = res.data.propertiList,
                    permissions = res.data.permissions,
                )
                is ApiResult.Failure -> {
                    if (res.error.isUnauthorized || res.error.isForbiddenBlocked) {
                        repository.sessionStore.clearSession()
                        _state.value = SessionUiState.Unauthenticated(bannerMessage = res.error.message)
                    } else {
                        _state.value = SessionUiState.Unauthenticated(bannerMessage = res.error.message)
                    }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = SessionUiState.Unauthenticated(isLoggingIn = true)
            when (val res = repository.login(username, password)) {
                is ApiResult.Success -> applyMeResult(
                    user = res.data.user,
                    propertiAktif = res.data.propertiAktif,
                    propertiList = res.data.propertiList,
                    permissions = res.data.permissions,
                )
                is ApiResult.Failure -> {
                    _state.value = SessionUiState.Unauthenticated(
                        isLoggingIn = false,
                        loginError = res.error,
                    )
                }
            }
        }
    }

    fun selectProperti(propertiId: Int) {
        viewModelScope.launch {
            when (val current = _state.value) {
                is SessionUiState.SelectProperti -> {
                    _state.value = current.copy(isSubmitting = true, message = null)
                }
                is SessionUiState.Authenticated -> {
                    _state.value = current.copy(isSwitchingProperti = true)
                }
                else -> Unit
            }

            when (val res = repository.selectProperti(propertiId)) {
                is ApiResult.Success -> applyMeResult(
                    user = res.data.user,
                    propertiAktif = res.data.propertiAktif,
                    propertiList = res.data.propertiList,
                    permissions = res.data.permissions,
                )
                is ApiResult.Failure -> {
                    when (val current = _state.value) {
                        is SessionUiState.SelectProperti -> {
                            _state.value = current.copy(isSubmitting = false, message = res.error.message)
                        }
                        is SessionUiState.Authenticated -> {
                            _state.value = current.copy(isSwitchingProperti = false, showPropertiSwitcher = false)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    fun openPropertiSwitcher() {
        _state.update { current ->
            if (current is SessionUiState.Authenticated && current.propertiList.size > 1) {
                current.copy(showPropertiSwitcher = true)
            } else {
                current
            }
        }
    }

    fun dismissPropertiSwitcher() {
        _state.update { current ->
            if (current is SessionUiState.Authenticated) {
                current.copy(showPropertiSwitcher = false)
            } else {
                current
            }
        }
    }

    fun updateDebugBaseUrl(url: String) {
        viewModelScope.launch {
            repository.sessionStore.saveDebugBaseUrl(url)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _state.value = SessionUiState.Unauthenticated(bannerMessage = "Anda telah keluar dari akun.")
        }
    }

    private fun applyMeResult(
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
                    "Pilih properti aktif terlebih dahulu."
                },
            )
        } else {
            _state.value = SessionUiState.Authenticated(
                user = user,
                propertiAktif = propertiAktif,
                propertiList = propertiList,
                permissions = permissions,
            )
        }
    }

    class Factory(private val repository: LaundryKosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(repository) as T
    }
}

package com.laundry_kos.feature.pengguna

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.data.dto.PenggunaListResponseDto
import com.laundry_kos.data.dto.ResetPasswordRequestDto
import com.laundry_kos.data.dto.SimpanPenggunaRequestDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PenggunaUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val submitting: Boolean = false,
    val data: PenggunaListResponseDto? = null,
    val error: String? = null,
    val snackbarMessage: String? = null,
)

class PenggunaViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PenggunaUiState())
    val state: StateFlow<PenggunaUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = !isRefresh && it.data == null,
                    refreshing = isRefresh,
                    error = null,
                )
            }
            when (val res = repository.fetchPenggunaList()) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, refreshing = false, data = res.data, error = null)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, refreshing = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun tambahPengguna(request: SimpanPenggunaRequestDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            when (val res = repository.tambahPengguna(request)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false, snackbarMessage = res.data.message) }
                    onSuccess()
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submitting = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    fun toggleAktif(userId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            when (val res = repository.toggleAktifPengguna(userId)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false, snackbarMessage = res.data.message) }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submitting = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    fun resetPassword(userId: Int, password: String, confirmation: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true) }
            when (val res = repository.resetPasswordPengguna(userId, ResetPasswordRequestDto(password, confirmation))) {
                is ApiResult.Success -> {
                    _state.update { it.copy(submitting = false, snackbarMessage = res.data.message) }
                    onSuccess()
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submitting = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    class Factory(private val repository: LaundryKosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PenggunaViewModel(repository) as T
    }
}

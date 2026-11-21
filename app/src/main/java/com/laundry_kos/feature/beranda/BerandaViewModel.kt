package com.laundry_kos.feature.beranda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.BerandaResponseDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BerandaUiState(
    val bulan: String = DateUtil.currentYearMonth(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val data: BerandaResponseDto? = null,
    val error: String? = null,
)

class BerandaViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BerandaUiState())
    val state: StateFlow<BerandaUiState> = _state.asStateFlow()

    init {
        load(isRefresh = false)
    }

    fun changeBulan(newBulan: String) {
        if (newBulan == _state.value.bulan) return
        _state.update { it.copy(bulan = newBulan) }
        load(isRefresh = false)
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
            when (val res = repository.fetchBeranda(_state.value.bulan)) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        data = res.data,
                        error = null,
                    )
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = res.error.summaryMessage(),
                    )
                }
            }
        }
    }

    class Factory(private val repository: LaundryKosRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            BerandaViewModel(repository) as T
    }
}

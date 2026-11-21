package com.laundry_kos.feature.penghuni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.KamarDto
import com.laundry_kos.data.dto.MasukPenghuniRequestDto
import com.laundry_kos.data.dto.PenghuniListResponseDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PenghuniListUiState(
    val tab: String = "semua",
    val cari: String = "",
    val bulan: String = DateUtil.currentYearMonth(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val data: PenghuniListResponseDto? = null,
    val error: String? = null,
    val snackbarMessage: String? = null,
    // Dialog daftar penghuni baru
    val showTambahDialog: Boolean = false,
    val kamarKosong: List<KamarDto> = emptyList(),
    val loadingKamar: Boolean = false,
    val submittingTambah: Boolean = false,
    val tambahError: String? = null,
)

class PenghuniListViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PenghuniListUiState())
    val state: StateFlow<PenghuniListUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun setTab(tab: String) {
        if (tab == _state.value.tab) return
        _state.update { it.copy(tab = tab) }
        load()
    }

    fun setBulan(bulan: String) {
        if (bulan == _state.value.bulan) return
        _state.update { it.copy(bulan = bulan) }
        load()
    }

    fun setCari(query: String) {
        _state.update { it.copy(cari = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun load(isRefresh: Boolean = false) {
        val s = _state.value
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = !isRefresh && it.data == null,
                    refreshing = isRefresh,
                    error = null,
                )
            }
            when (val res = repository.fetchPenghuniList(tab = s.tab, cari = s.cari, bulan = s.bulan)) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, refreshing = false, data = res.data, error = null)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, refreshing = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun openTambahDialog() {
        _state.update { it.copy(showTambahDialog = true, loadingKamar = true, tambahError = null) }
        viewModelScope.launch {
            when (val res = repository.fetchKamar(hanyaKosong = true)) {
                is ApiResult.Success -> _state.update {
                    it.copy(loadingKamar = false, kamarKosong = res.data.data)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loadingKamar = false, tambahError = res.error.summaryMessage())
                }
            }
        }
    }

    fun dismissTambahDialog() {
        _state.update { it.copy(showTambahDialog = false, tambahError = null) }
    }

    fun submitTambahPenghuni(request: MasukPenghuniRequestDto) {
        viewModelScope.launch {
            _state.update { it.copy(submittingTambah = true, tambahError = null) }
            when (val res = repository.tambahPenghuni(request)) {
                is ApiResult.Success -> {
                    val msg = buildString {
                        append(res.data.message)
                        res.data.peringatan?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
                    }
                    _state.update {
                        it.copy(
                            submittingTambah = false,
                            showTambahDialog = false,
                            snackbarMessage = msg,
                        )
                    }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submittingTambah = false, tambahError = res.error.summaryMessage())
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
            PenghuniListViewModel(repository) as T
    }
}

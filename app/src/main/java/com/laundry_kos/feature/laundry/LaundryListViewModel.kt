package com.laundry_kos.feature.laundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.InfoKuotaPenghuniDto
import com.laundry_kos.data.dto.LaundryDto
import com.laundry_kos.data.dto.LaundryListResponseDto
import com.laundry_kos.data.dto.TerhapusListResponseDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LaundryListUiState(
    val periode: String = "bulan_ini",
    val dari: String = DateUtil.todayIso(),
    val sampai: String = DateUtil.todayIso(),
    val jenis: String = "",
    val cari: String = "",
    val tampil: Int = 20,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val data: LaundryListResponseDto? = null,
    val error: String? = null,
    val snackbarMessage: String? = null,
    val actionInProgress: Boolean = false,
    // Dialog Terhapus
    val showTerhapusDialog: Boolean = false,
    val terhapusLoading: Boolean = false,
    val terhapusTampil: Int = 15,
    val terhapusData: TerhapusListResponseDto? = null,
    val terhapusError: String? = null,
    // Dialog Tetapkan Pemilik
    val tetapkanTarget: LaundryDto? = null,
    val tetapkanPenghuniList: List<InfoKuotaPenghuniDto> = emptyList(),
    val tetapkanLoading: Boolean = false,
    val tetapkanError: String? = null,
)

class LaundryListViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LaundryListUiState())
    val state: StateFlow<LaundryListUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        load()
    }

    fun setPeriode(periode: String) {
        _state.update { it.copy(periode = periode, tampil = 20) }
        load()
    }

    fun setRentang(dari: String, sampai: String) {
        _state.update { it.copy(periode = "rentang", dari = dari, sampai = sampai, tampil = 20) }
        load()
    }

    fun setJenis(jenis: String) {
        _state.update { it.copy(jenis = jenis, tampil = 20) }
        load()
    }

    fun setCari(query: String) {
        _state.update { it.copy(cari = query, tampil = 20) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            load()
        }
    }

    fun loadMore() {
        val meta = _state.value.data?.meta ?: return
        if (!meta.adaLagi || _state.value.loadingMore) return
        _state.update { it.copy(tampil = meta.tampilBerikutnya, loadingMore = true) }
        load(isLoadMore = true)
    }

    fun load(isRefresh: Boolean = false, isLoadMore: Boolean = false) {
        val s = _state.value
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = !isRefresh && !isLoadMore && it.data == null,
                    refreshing = isRefresh,
                    loadingMore = isLoadMore,
                    error = null,
                )
            }
            val res = repository.fetchLaundryList(
                periode = s.periode,
                dari = if (s.periode == "rentang") s.dari else null,
                sampai = if (s.periode == "rentang") s.sampai else null,
                cari = s.cari,
                jenis = s.jenis,
                tampil = s.tampil,
            )
            when (res) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        data = res.data,
                        error = null,
                    )
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        loadingMore = false,
                        error = res.error.summaryMessage(),
                    )
                }
            }
        }
    }

    fun hapusLaundry(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            when (val res = repository.hapusLaundry(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionInProgress = false, snackbarMessage = res.data.message) }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(actionInProgress = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    fun lengkapiHarga() {
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            when (val res = repository.lengkapiHarga()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(actionInProgress = false, snackbarMessage = res.data.message) }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(actionInProgress = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    fun openTerhapusDialog() {
        _state.update { it.copy(showTerhapusDialog = true, terhapusTampil = 15, terhapusError = null) }
        loadTerhapus()
    }

    fun closeTerhapusDialog() {
        _state.update { it.copy(showTerhapusDialog = false, terhapusError = null) }
    }

    fun loadMoreTerhapus() {
        val next = _state.value.terhapusData?.meta?.tampilBerikutnya ?: return
        _state.update { it.copy(terhapusTampil = next) }
        loadTerhapus()
    }

    private fun loadTerhapus() {
        viewModelScope.launch {
            _state.update { it.copy(terhapusLoading = true, terhapusError = null) }
            when (val res = repository.fetchLaundryTerhapus(_state.value.terhapusTampil)) {
                is ApiResult.Success -> _state.update {
                    it.copy(terhapusLoading = false, terhapusData = res.data)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(terhapusLoading = false, terhapusError = res.error.summaryMessage())
                }
            }
        }
    }

    fun pulihkan(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(terhapusLoading = true, terhapusError = null) }
            when (val res = repository.pulihkanLaundry(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(snackbarMessage = res.data.message, terhapusError = null) }
                    loadTerhapus()
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(terhapusLoading = false, terhapusError = res.error.summaryMessage())
                }
            }
        }
    }

    fun openTetapkanPemilik(laundry: LaundryDto) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    tetapkanTarget = laundry,
                    tetapkanLoading = true,
                    tetapkanPenghuniList = emptyList(),
                    tetapkanError = null,
                )
            }
            when (val res = repository.fetchInfoKuota(tanggal = laundry.tanggal)) {
                is ApiResult.Success -> _state.update {
                    it.copy(tetapkanLoading = false, tetapkanPenghuniList = res.data.daftarPenghuni)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(tetapkanLoading = false, tetapkanError = res.error.summaryMessage())
                }
            }
        }
    }

    fun dismissTetapkanPemilik() {
        _state.update { it.copy(tetapkanTarget = null, tetapkanError = null) }
    }

    fun confirmTetapkanPemilik(penghuniId: Int) {
        val target = _state.value.tetapkanTarget ?: return
        viewModelScope.launch {
            _state.update { it.copy(tetapkanLoading = true, tetapkanError = null) }
            when (val res = repository.tetapkanPemilik(target.id, penghuniId)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            tetapkanTarget = null,
                            tetapkanLoading = false,
                            tetapkanError = null,
                            snackbarMessage = res.data.message,
                        )
                    }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(tetapkanLoading = false, tetapkanError = res.error.summaryMessage())
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
            LaundryListViewModel(repository) as T
    }
}

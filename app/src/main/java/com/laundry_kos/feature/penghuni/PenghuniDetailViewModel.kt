package com.laundry_kos.feature.penghuni

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.*
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PenghuniDetailUiState(
    val penghuniId: Int,
    val bulan: String = DateUtil.currentYearMonth(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val data: PenghuniDetailResponseDto? = null,
    val error: String? = null,
    val actionLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val kamarKosong: List<KamarDto> = emptyList(),
)

class PenghuniDetailViewModel(
    private val repository: LaundryKosRepository,
    private val penghuniId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(PenghuniDetailUiState(penghuniId = penghuniId))
    val state: StateFlow<PenghuniDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun changeBulan(newBulan: String) {
        _state.update { it.copy(bulan = newBulan) }
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
            when (val res = repository.fetchPenghuniDetail(penghuniId, _state.value.bulan)) {
                is ApiResult.Success -> _state.update {
                    it.copy(loading = false, refreshing = false, data = res.data, error = null)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, refreshing = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun loadKamarKosong() {
        viewModelScope.launch {
            val res = repository.fetchKamar(hanyaKosong = true)
            if (res is ApiResult.Success) {
                _state.update { it.copy(kamarKosong = res.data.data) }
            }
        }
    }

    fun ubahData(request: UbahPenghuniRequestDto) {
        runAction {
            val res = repository.ubahPenghuni(penghuniId, request)
            handleMutasi(res) { it.message }
        }
    }

    fun gantiNomor(noWa: String?) {
        runAction {
            val res = repository.gantiNomor(penghuniId, noWa)
            handleMutasi(res) { r ->
                buildString {
                    append(r.message)
                    r.peringatan?.takeIf { it.isNotBlank() }?.let { append(" ($it)") }
                }
            }
        }
    }

    fun pindahKamar(kamarTujuanId: Int, tanggalPindah: String) {
        runAction {
            val res = repository.pindahKamar(penghuniId, PindahKamarRequestDto(kamarTujuanId, tanggalPindah))
            handleMutasi(res) { it.message }
        }
    }

    fun batalkanPindah() {
        runAction {
            val res = repository.batalkanPindah(penghuniId)
            handleMutasi(res) { it.message }
        }
    }

    fun keluar(tanggalKeluar: String, alasan: String?) {
        runAction {
            val res = repository.keluarPenghuni(
                penghuniId,
                KeluarPenghuniRequestDto(tanggalKeluar, alasan?.takeIf { it.isNotBlank() }),
            )
            handleMutasi(res) { it.message }
        }
    }

    fun batalkanKeluar() {
        runAction {
            val res = repository.batalkanKeluar(penghuniId)
            handleMutasi(res) { it.message }
        }
    }

    fun simpanPenyesuaianKuota(nominal: Int?, reset: Boolean, catatan: String?) {
        runAction {
            val res = repository.simpanPenyesuaianKuota(
                penghuniId,
                PenyesuaianKuotaRequestDto(
                    bulan = _state.value.bulan,
                    nominal = if (reset) null else nominal,
                    reset = reset,
                    catatan = catatan?.takeIf { it.isNotBlank() },
                ),
            )
            handleMutasi(res) { it.message }
        }
    }

    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(actionLoading = true) }
            block()
            _state.update { it.copy(actionLoading = false) }
        }
    }

    private fun <T> handleMutasi(res: ApiResult<T>, messageOf: (T) -> String) {
        when (res) {
            is ApiResult.Success -> {
                _state.update { it.copy(snackbarMessage = messageOf(res.data)) }
                load(isRefresh = true)
            }
            is ApiResult.Failure -> {
                _state.update { it.copy(snackbarMessage = res.error.summaryMessage()) }
            }
        }
    }

    fun clearSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    class Factory(
        private val repository: LaundryKosRepository,
        private val penghuniId: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PenghuniDetailViewModel(repository, penghuniId) as T
    }
}

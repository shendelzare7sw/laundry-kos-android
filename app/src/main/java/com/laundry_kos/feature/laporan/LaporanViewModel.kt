package com.laundry_kos.feature.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.LaporanResponse
import com.laundry_kos.data.dto.PenyesuaianKuotaRequestDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LaporanUiState(
    val periode: String = "bulan_ini",
    val dari: String = DateUtil.todayIso(),
    val sampai: String = DateUtil.todayIso(),
    val rinci: Boolean = false,
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val data: LaporanResponse? = null,
    val error: String? = null,
    // Inline edit tagihan lewat kuota per penghuni_id
    val editingPenghuniId: Int? = null,
    val inlineNominal: String = "",
    val inlineSaving: Boolean = false,
    val snackbarMessage: String? = null,
)

class LaporanViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LaporanUiState())
    val state: StateFlow<LaporanUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun setPeriode(periode: String) {
        _state.update { it.copy(periode = periode, editingPenghuniId = null, inlineNominal = "") }
        load()
    }

    fun setRentang(dari: String, sampai: String) {
        _state.update {
            it.copy(
                periode = "rentang",
                dari = dari,
                sampai = sampai,
                editingPenghuniId = null,
                inlineNominal = "",
            )
        }
        load()
    }

    fun toggleRinci(rinci: Boolean) {
        _state.update { it.copy(rinci = rinci) }
        load()
    }

    fun load(isRefresh: Boolean = false) {
        val s = _state.value
        viewModelScope.launch {
            _state.update {
                it.copy(
                    loading = !isRefresh && it.data == null,
                    refreshing = isRefresh,
                    editingPenghuniId = null,
                    inlineNominal = "",
                    error = null,
                )
            }
            val res = repository.fetchLaporan(
                periode = s.periode,
                dari = if (s.periode == "rentang") s.dari else null,
                sampai = if (s.periode == "rentang") s.sampai else null,
                rinci = s.rinci,
            )
            when (res) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        editingPenghuniId = null,
                        inlineNominal = "",
                        data = res.data,
                        error = null,
                    )
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, refreshing = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun startInlineEdit(penghuniId: Int, currentTagihan: Int) {
        _state.update {
            it.copy(
                editingPenghuniId = penghuniId,
                inlineNominal = currentTagihan.toString(),
            )
        }
    }

    fun updateInlineNominal(value: String) {
        _state.update { it.copy(inlineNominal = value.filter(Char::isDigit)) }
    }

    fun cancelInlineEdit() {
        _state.update { it.copy(editingPenghuniId = null, inlineNominal = "") }
    }

    fun saveInlinePenyesuaian(penghuniId: Int, bulan: String, reset: Boolean) {
        if (_state.value.inlineSaving) return
        val nominal = _state.value.inlineNominal.toIntOrNull()
        if (!reset && nominal == null) return
        viewModelScope.launch {
            _state.update { it.copy(inlineSaving = true) }
            val res = repository.simpanPenyesuaianKuota(
                penghuniId = penghuniId,
                request = PenyesuaianKuotaRequestDto(
                    bulan = bulan,
                    nominal = if (reset) null else nominal,
                    reset = reset,
                ),
            )
            when (res) {
                is ApiResult.Success -> {
                    val updatedKuota = res.data.kuota
                    _state.update { cur ->
                        // Segera tutup form inline dan perbarui nominal di kartu agar tidak ada jeda
                        val patchedData = cur.data?.let { lap ->
                            lap.copy(
                                kartuPenghuni = lap.kartuPenghuni.map { kartu ->
                                    if (kartu.penghuni.id == penghuniId) {
                                        kartu.copy(
                                            tagihanLewatKuota = updatedKuota.tagihanLewatKuota,
                                            kuota = kartu.kuota.copy(
                                                tagihanLewatKuota = updatedKuota.tagihanLewatKuota,
                                                nominalPenyesuaian = updatedKuota.nominalPenyesuaian,
                                                totalTagihan = updatedKuota.totalTagihan,
                                            ),
                                        )
                                    } else {
                                        kartu
                                    }
                                },
                            )
                        }
                        cur.copy(
                            inlineSaving = false,
                            editingPenghuniId = null,
                            inlineNominal = "",
                            data = patchedData,
                            snackbarMessage = res.data.message,
                        )
                    }
                    load(isRefresh = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(
                        inlineSaving = false,
                        snackbarMessage = res.error.summaryMessage(),
                    )
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
            LaporanViewModel(repository) as T
    }
}

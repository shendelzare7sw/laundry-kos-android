package com.laundry_kos.feature.pengaturan

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

data class PengaturanUiState(
    val tanggalAcuan: String = DateUtil.todayIso(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val bolehUbah: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null,
    // Form Aturan
    val berlakuMulai: String = DateUtil.todayIso(),
    val namaProperti: String = "",
    val tarifPerKg: String = "8000",
    val minKg: String = "2",
    val kuotaKg: String = "20",
    val ambangPersen: String = "75",
    val toleransiKg: String = "1",
    val pembulatan: Int = 1000,
    val modeKuota: String = "penghuni",
    val kuotaProrata: Boolean = false,
    // Form Izin Penjaga
    val jamEditPenjaga: String = "24",
    val penjagaKelolaKontrak: Boolean = false,
    val penjagaUbahCatatanLain: Boolean = false,
    // Form Template WA
    val templateWaTagihan: String = "",
    val templateWaPeringatan: String = "",
    val templateWaRekap: String = "",
)

class PengaturanViewModel(
    private val repository: LaundryKosRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PengaturanUiState())
    val state: StateFlow<PengaturanUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(tanggal: String = _state.value.tanggalAcuan) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, tanggalAcuan = tanggal, error = null) }
            when (val res = repository.fetchPengaturan(tanggal)) {
                is ApiResult.Success -> {
                    val d = res.data
                    _state.update {
                        it.copy(
                            loading = false,
                            tanggalAcuan = d.tanggalAcuan.ifBlank { tanggal },
                            bolehUbah = d.bolehUbah,
                            berlakuMulai = DateUtil.todayIso(),
                            namaProperti = d.aturan.namaProperti,
                            tarifPerKg = d.aturan.tarifPerKg.toString(),
                            minKg = d.aturan.minKg,
                            kuotaKg = d.aturan.kuotaKg,
                            ambangPersen = d.aturan.ambangMendekatiPersen.toString(),
                            toleransiKg = d.aturan.toleransiKg,
                            pembulatan = d.aturan.pembulatan,
                            modeKuota = d.aturan.modeKuota,
                            kuotaProrata = d.aturan.kuotaProrataBulanMasuk,
                            jamEditPenjaga = d.izinPenjaga.jamEditPenjaga?.toString() ?: "",
                            penjagaKelolaKontrak = d.izinPenjaga.penjagaKelolaKontrak,
                            penjagaUbahCatatanLain = d.izinPenjaga.penjagaUbahCatatanLain,
                            templateWaTagihan = d.templateWa.templateWaTagihan,
                            templateWaPeringatan = d.templateWa.templateWaPeringatan,
                            templateWaRekap = d.templateWa.templateWaRekap,
                        )
                    }
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun updateState(transform: (PengaturanUiState) -> PengaturanUiState) {
        _state.update(transform)
    }

    fun simpanAturan() {
        val s = _state.value
        val req = SimpanAturanRequestDto(
            berlakuMulai = s.berlakuMulai,
            namaProperti = s.namaProperti.trim(),
            tarifPerKg = s.tarifPerKg.toIntOrNull() ?: 8000,
            minKg = s.minKg.replace(',', '.').trim(),
            kuotaKg = s.kuotaKg.replace(',', '.').trim(),
            ambangMendekatiPersen = s.ambangPersen.toIntOrNull() ?: 75,
            toleransiKg = s.toleransiKg.replace(',', '.').trim(),
            pembulatan = s.pembulatan,
            modeKuota = s.modeKuota,
            kuotaProrataBulanMasuk = s.kuotaProrata,
        )
        runSave { repository.simpanAturan(req) }
    }

    fun simpanIzinPenjaga() {
        val s = _state.value
        val req = SimpanIzinPenjagaRequestDto(
            jamEditPenjaga = s.jamEditPenjaga.trim().takeIf { it.isNotEmpty() }?.toIntOrNull(),
            penjagaKelolaKontrak = s.penjagaKelolaKontrak,
            penjagaUbahCatatanLain = s.penjagaUbahCatatanLain,
        )
        runSave { repository.simpanIzinPenjaga(req) }
    }

    fun simpanTemplateWa() {
        val s = _state.value
        val req = SimpanTemplateWaRequestDto(
            templateWaTagihan = s.templateWaTagihan,
            templateWaPeringatan = s.templateWaPeringatan,
            templateWaRekap = s.templateWaRekap,
        )
        runSave { repository.simpanTemplateWa(req) }
    }

    fun resetTemplateWa(kunci: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val res = repository.resetTemplateWa(kunci)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, snackbarMessage = res.data.message) }
                    load()
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(saving = false, snackbarMessage = res.error.summaryMessage())
                }
            }
        }
    }

    private fun runSave(call: suspend () -> ApiResult<MessageResponseDto>) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val res = call()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, snackbarMessage = res.data.message) }
                    load()
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(saving = false, snackbarMessage = res.error.summaryMessage())
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
            PengaturanViewModel(repository) as T
    }
}

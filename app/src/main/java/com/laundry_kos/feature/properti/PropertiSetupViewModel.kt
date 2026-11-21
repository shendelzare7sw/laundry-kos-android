package com.laundry_kos.feature.properti

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.data.dto.PropertiSetupResponseDto
import com.laundry_kos.data.dto.SetupAturanDto
import com.laundry_kos.data.dto.WizardKamarRangeDto
import com.laundry_kos.data.dto.WizardKamarRequestDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PropertiSetupUiState(
    val propertiId: Int,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val data: PropertiSetupResponseDto? = null,
    val error: String? = null,
    val snackbarMessage: String? = null,
    // Form generate kamar
    val lantaiInput: String = "1",
    val dariInput: String = "101",
    val sampaiInput: String = "110",
    // Form aturan awal
    val tarifPerKg: String = "8000",
    val minKg: String = "2",
    val kuotaKg: String = "20",
    val ambangPersen: String = "75",
    val toleransiKg: String = "1",
    val pembulatan: Int = 1000,
    val modeKuota: String = "penghuni",
    val jamEditPenjaga: String = "24",
    val penjagaKelolaKontrak: Boolean = false,
)

class PropertiSetupViewModel(
    private val repository: LaundryKosRepository,
    private val propertiId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(PropertiSetupUiState(propertiId = propertiId))
    val state: StateFlow<PropertiSetupUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val res = repository.fetchSetupProperti(propertiId)) {
                is ApiResult.Success -> {
                    val d = res.data
                    val a = d.aturan
                    val nextFloor = (d.kamarPerLantai.maxOfOrNull { it.lantai } ?: 0) + 1
                    _state.update {
                        it.copy(
                            loading = false,
                            data = d,
                            error = null,
                            lantaiInput = nextFloor.toString(),
                            dariInput = "${nextFloor}01",
                            sampaiInput = "${nextFloor}10",
                            tarifPerKg = a.tarifPerKg.toString(),
                            minKg = a.minKg,
                            kuotaKg = a.kuotaKg,
                            ambangPersen = a.ambangMendekatiPersen.toString(),
                            toleransiKg = a.toleransiKg,
                            pembulatan = a.pembulatan,
                            modeKuota = a.modeKuota,
                            jamEditPenjaga = a.jamEditPenjaga.toString(),
                            penjagaKelolaKontrak = a.penjagaKelolaKontrak,
                        )
                    }
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, error = res.error.summaryMessage())
                }
            }
        }
    }

    fun update(transform: (PropertiSetupUiState) -> PropertiSetupUiState) {
        _state.update(transform)
    }

    fun generateKamar() {
        val s = _state.value
        val lantai = s.lantaiInput.toIntOrNull() ?: return
        val dari = s.dariInput.toIntOrNull() ?: return
        val sampai = s.sampaiInput.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val req = WizardKamarRequestDto(
                lantai = listOf(WizardKamarRangeDto(lantai = lantai, dari = dari, sampai = sampai)),
            )
            when (val res = repository.setupKamar(propertiId, req)) {
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

    fun simpanAturan() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            val req = SetupAturanDto(
                tarifPerKg = s.tarifPerKg.toIntOrNull() ?: 8000,
                minKg = s.minKg.replace(',', '.').trim(),
                kuotaKg = s.kuotaKg.replace(',', '.').trim(),
                ambangMendekatiPersen = s.ambangPersen.toIntOrNull() ?: 75,
                toleransiKg = s.toleransiKg.replace(',', '.').trim(),
                pembulatan = s.pembulatan,
                modeKuota = s.modeKuota,
                jamEditPenjaga = s.jamEditPenjaga.toIntOrNull() ?: 24,
                penjagaKelolaKontrak = s.penjagaKelolaKontrak,
            )
            when (val res = repository.setupAturan(propertiId, req)) {
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

    fun aktifkanProperti() {
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            when (val res = repository.setupAktifkan(propertiId)) {
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

    class Factory(
        private val repository: LaundryKosRepository,
        private val propertiId: Int,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PropertiSetupViewModel(repository, propertiId) as T
    }
}

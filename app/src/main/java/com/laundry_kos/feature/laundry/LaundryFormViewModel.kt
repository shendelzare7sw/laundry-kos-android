package com.laundry_kos.feature.laundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.network.ApiError
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.data.dto.InfoKuotaPenghuniDto
import com.laundry_kos.data.dto.InfoKuotaResponseDto
import com.laundry_kos.data.dto.SimpanLaundryItemDto
import com.laundry_kos.data.dto.SimpanLaundryRequestDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LaundryItemDraft(
    val uid: String = UUID.randomUUID().toString(),
    val nama: String = "",
    val jumlah: String = "1",
    val beratKg: String = "",
    val biaya: String = "",
)

data class LaundryFormUiState(
    val editId: Int? = null,
    val loadingInitial: Boolean = false,
    val jenis: String = "penghuni",
    val tanggal: String = DateUtil.todayIso(),
    val selectedPenghuniId: Int? = null,
    val selectedPenghuniLabel: String = "",
    val beratKg: String = "",
    val biayaManualMode: Boolean = false,
    val biayaUtamaInput: String = "",
    val konfirmasiBeratBesar: Boolean = false,
    val catatan: String = "",
    val items: List<LaundryItemDraft> = emptyList(),
    // UUID idempotensi per sesi form (dipertahankan pada retry)
    val idempotencyRef: String = UUID.randomUUID().toString(),
    // Live info kuota dari /laundry/info-kuota
    val loadingPreview: Boolean = false,
    val preview: InfoKuotaResponseDto? = null,
    val daftarPenghuni: List<InfoKuotaPenghuniDto> = emptyList(),
    // Submit state
    val submitting: Boolean = false,
    val submitError: ApiError? = null,
    val savedMessage: String? = null,
) {
    val isBeratBesar: Boolean
        get() {
            val cleaned = beratKg.replace(',', '.').trim()
            val parts = cleaned.split('.')
            val utuh = parts.firstOrNull()?.toIntOrNull() ?: return false
            val frac = parts.getOrNull(1)?.padEnd(2, '0')?.take(2)?.toIntOrNull() ?: 0
            return (utuh * 100 + frac) > 3000
        }

    val hasDuplikatBlock: Boolean
        get() = jenis == "penghuni" && preview?.duplikat != null
}

class LaundryFormViewModel(
    private val repository: LaundryKosRepository,
    private val editId: Int?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LaundryFormUiState(
            editId = editId,
            loadingInitial = editId != null,
        ),
    )
    val state: StateFlow<LaundryFormUiState> = _state.asStateFlow()

    private var previewJob: Job? = null

    init {
        if (editId != null) {
            loadExisting(editId)
        } else {
            refreshInfoKuota(immediate = true)
        }
    }

    private fun loadExisting(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loadingInitial = true, submitError = null) }
            when (val res = repository.fetchLaundryDetail(id)) {
                is ApiResult.Success -> {
                    val d = res.data.data
                    _state.update {
                        it.copy(
                            loadingInitial = false,
                            jenis = d.jenis,
                            tanggal = d.tanggal,
                            selectedPenghuniId = d.penghuniId,
                            selectedPenghuniLabel = buildString {
                                d.kamarNomor?.let { k -> append("Kamar $k · ") }
                                append(d.penghuniNama ?: "")
                            },
                            beratKg = d.beratLabel?.replace(',', '.') ?: "",
                            biayaManualMode = d.biayaManual,
                            biayaUtamaInput = if (d.biayaManual) (d.biayaUtama?.toString() ?: "") else "",
                            catatan = d.catatan ?: "",
                            items = d.item.map { sub ->
                                LaundryItemDraft(
                                    nama = sub.nama,
                                    jumlah = sub.jumlah.toString(),
                                    beratKg = sub.beratLabel?.replace(',', '.') ?: "",
                                    biaya = sub.biaya.toString(),
                                )
                            },
                        )
                    }
                    refreshInfoKuota(immediate = true)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(loadingInitial = false, submitError = res.error)
                }
            }
        }
    }

    fun setJenis(jenis: String) {
        _state.update {
            it.copy(
                jenis = jenis,
                submitError = null,
            )
        }
        refreshInfoKuota(immediate = true)
    }

    fun setTanggal(tanggalIso: String) {
        _state.update { it.copy(tanggal = tanggalIso, submitError = null) }
        refreshInfoKuota(immediate = true)
    }

    fun selectPenghuni(penghuni: InfoKuotaPenghuniDto) {
        _state.update {
            it.copy(
                selectedPenghuniId = penghuni.id,
                selectedPenghuniLabel = penghuni.label.ifBlank { "Kamar ${penghuni.kamar} · ${penghuni.nama}" },
                submitError = null,
            )
        }
        refreshInfoKuota(immediate = true)
    }

    fun setBeratKg(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
        _state.update { it.copy(beratKg = filtered, submitError = null) }
        refreshInfoKuota(immediate = false)
    }

    fun setBiayaManualMode(manual: Boolean) {
        _state.update {
            val defaultManual = if (manual && it.biayaUtamaInput.isBlank()) {
                it.preview?.biayaOtomatis?.toString() ?: ""
            } else {
                it.biayaUtamaInput
            }
            it.copy(
                biayaManualMode = manual,
                biayaUtamaInput = if (manual) defaultManual else "",
                submitError = null,
            )
        }
    }

    fun setBiayaUtamaInput(value: String) {
        _state.update {
            it.copy(
                biayaManualMode = true,
                biayaUtamaInput = value.filter { c -> c.isDigit() },
                submitError = null,
            )
        }
    }

    fun setKonfirmasiBeratBesar(checked: Boolean) {
        _state.update { it.copy(konfirmasiBeratBesar = checked, submitError = null) }
    }

    fun setCatatan(value: String) {
        _state.update { it.copy(catatan = value) }
    }

    fun addItem() {
        _state.update { it.copy(items = it.items + LaundryItemDraft()) }
    }

    fun removeItem(uid: String) {
        _state.update { it.copy(items = it.items.filterNot { item -> item.uid == uid }) }
    }

    fun updateItem(uid: String, transform: (LaundryItemDraft) -> LaundryItemDraft) {
        _state.update {
            it.copy(
                items = it.items.map { item -> if (item.uid == uid) transform(item) else item },
                submitError = null,
            )
        }
    }

    private fun refreshInfoKuota(immediate: Boolean) {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            if (!immediate) delay(350)
            val s = _state.value
            _state.update { it.copy(loadingPreview = true) }
            val normalizedBerat = s.beratKg.replace(',', '.').trim().takeIf { it.isNotEmpty() }
            val res = repository.fetchInfoKuota(
                tanggal = s.tanggal,
                penghuniId = if (s.jenis == "penghuni") s.selectedPenghuniId else null,
                beratKg = normalizedBerat,
                kecualiId = s.editId,
            )
            when (res) {
                is ApiResult.Success -> _state.update { cur ->
                    val list = res.data.daftarPenghuni
                    val selectedStillInList = list.firstOrNull { it.id == cur.selectedPenghuniId }
                    cur.copy(
                        loadingPreview = false,
                        preview = res.data,
                        daftarPenghuni = list,
                        selectedPenghuniLabel = selectedStillInList?.let {
                            it.label.ifBlank { "Kamar ${it.kamar} · ${it.nama}" }
                        } ?: cur.selectedPenghuniLabel,
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(loadingPreview = false) }
            }
        }
    }

    fun submit() {
        val s = _state.value
        if (s.submitting) return

        val itemDtos = s.items.mapNotNull { draft ->
            if (draft.nama.isBlank() && draft.biaya.isBlank() && draft.beratKg.isBlank()) {
                null
            } else {
                SimpanLaundryItemDto(
                    nama = draft.nama.trim(),
                    jumlah = draft.jumlah.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    beratKg = draft.beratKg.replace(',', '.').trim().takeIf { it.isNotEmpty() },
                    biaya = draft.biaya.toIntOrNull() ?: 0,
                )
            }
        }

        val request = SimpanLaundryRequestDto(
            jenis = s.jenis,
            tanggal = s.tanggal,
            penghuniId = if (s.jenis == "penghuni") s.selectedPenghuniId else null,
            beratKg = s.beratKg.replace(',', '.').trim().takeIf { it.isNotEmpty() },
            biayaUtama = if (s.biayaManualMode) s.biayaUtamaInput.toIntOrNull() else null,
            konfirmasiBeratBesar = s.konfirmasiBeratBesar,
            catatan = s.catatan.trim().takeIf { it.isNotEmpty() },
            ref = if (s.editId == null) s.idempotencyRef else null,
            item = itemDtos,
        )

        viewModelScope.launch {
            _state.update { it.copy(submitting = true, submitError = null) }
            val res = if (s.editId != null) {
                repository.ubahLaundry(s.editId, request)
            } else {
                repository.tambahLaundry(request)
            }
            when (res) {
                is ApiResult.Success -> _state.update {
                    it.copy(submitting = false, savedMessage = res.data.message)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(submitting = false, submitError = res.error)
                }
            }
        }
    }

    class Factory(
        private val repository: LaundryKosRepository,
        private val editId: Int?,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LaundryFormViewModel(repository, editId) as T
    }
}

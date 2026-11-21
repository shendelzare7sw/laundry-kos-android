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
    val bolehUbahExisting: Boolean = true,
    val loadingInitial: Boolean = false,
    val jenis: String = "penghuni",
    val tanggal: String = DateUtil.todayIso(),
    val selectedPenghuniId: Int? = null,
    val selectedPenghuniLabel: String = "",
    val namaTanpaNama: String = "",
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
                            bolehUbahExisting = d.bolehUbah,
                            jenis = d.jenis,
                            tanggal = d.tanggal,
                            selectedPenghuniId = d.penghuniId,
                            selectedPenghuniLabel = buildString {
                                d.kamarNomor?.let { k -> append("Kamar $k · ") }
                                append(d.penghuniNama ?: "")
                            },
                            namaTanpaNama = d.namaTanpaNama ?: "",
                            beratKg = d.beratLabel?.replace(',', '.') ?: "",
                            biayaManualMode = d.biayaManual,
                            biayaUtamaInput = d.biayaUtama?.toString() ?: "",
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

    fun selectPenghuni(penghuni: InfoKuotaPenghuniDto?) {
        if (penghuni == null) {
            _state.update {
                it.copy(
                    selectedPenghuniId = null,
                    selectedPenghuniLabel = "",
                    preview = it.preview?.copy(kuota = null, galat = null, duplikat = null),
                    submitError = null,
                )
            }
            return
        }
        _state.update {
            it.copy(
                selectedPenghuniId = penghuni.id,
                selectedPenghuniLabel = penghuni.label.ifBlank { "Kamar ${penghuni.kamar} · ${penghuni.nama}" },
                submitError = null,
            )
        }
        refreshInfoKuota(immediate = true)
    }

    fun setNamaTanpaNama(value: String) {
        _state.update { it.copy(namaTanpaNama = value.take(120), submitError = null) }
    }

    fun setBeratKg(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
        _state.update {
            it.copy(
                beratKg = filtered,
                biayaUtamaInput = if (filtered.isBlank() && !it.biayaManualMode) "" else it.biayaUtamaInput,
                submitError = null,
            )
        }
        refreshInfoKuota(immediate = false)
    }

    fun setBiayaUtamaInput(value: String) {
        val digits = value.filter(Char::isDigit)
        _state.update { cur ->
            val autoVal = cur.preview?.biayaOtomatis
            val isManual = if (digits.isBlank()) {
                false
            } else {
                autoVal == null || digits.toIntOrNull() != autoVal
            }
            cur.copy(
                biayaManualMode = isManual,
                biayaUtamaInput = digits,
                submitError = null,
            )
        }
    }

    fun kembalikanBiayaOtomatis() {
        _state.update { cur ->
            cur.copy(
                biayaManualMode = false,
                biayaUtamaInput = cur.preview?.biayaOtomatis?.toString() ?: "",
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
            if (!immediate) delay(200)
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
                    val autoCost = res.data.biayaOtomatis
                    val nextBiaya = when {
                        cur.beratKg.isBlank() && !cur.biayaManualMode -> ""
                        !cur.biayaManualMode || cur.biayaUtamaInput.isBlank() -> autoCost?.toString() ?: ""
                        else -> cur.biayaUtamaInput
                    }
                    cur.copy(
                        loadingPreview = false,
                        preview = res.data,
                        daftarPenghuni = list,
                        biayaUtamaInput = nextBiaya,
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

        if (s.editId != null && !s.bolehUbahExisting) {
            _state.update {
                it.copy(
                    submitError = ApiError(
                        httpCode = 403,
                        message = "Catatan laundry ini terkunci (dicatat staf lain atau melewati batas jam edit penjaga).",
                    ),
                )
            }
            return
        }

        if (s.jenis == "penghuni" && s.selectedPenghuniId == null) {
            _state.update {
                it.copy(
                    submitError = ApiError(
                        httpCode = 422,
                        message = "Penghuni belum dipilih untuk catatan laundry penghuni.",
                        fieldErrors = mapOf("penghuni_id" to listOf("Penghuni belum dipilih untuk catatan laundry penghuni.")),
                    ),
                )
            }
            return
        }

        if (s.hasDuplikatBlock) {
            val msg = s.preview?.galat ?: "Penghuni sudah memiliki catatan laundry pada tanggal ini."
            _state.update {
                it.copy(
                    submitError = ApiError(
                        httpCode = 422,
                        message = msg,
                        fieldErrors = mapOf("tanggal" to listOf(msg)),
                    ),
                )
            }
            return
        }

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

        val beratNorm = s.beratKg.replace(',', '.').trim().takeIf { it.isNotEmpty() }
        if (beratNorm == null && itemDtos.isEmpty()) {
            val msg = "Isi berat cucian utama atau tambahkan minimal satu item terpisah."
            _state.update {
                it.copy(
                    submitError = ApiError(
                        httpCode = 422,
                        message = msg,
                        fieldErrors = mapOf("berat_kg" to listOf(msg)),
                    ),
                )
            }
            return
        }

        val request = SimpanLaundryRequestDto(
            jenis = s.jenis,
            tanggal = s.tanggal,
            penghuniId = if (s.jenis == "penghuni") s.selectedPenghuniId else null,
            namaTanpaNama = if (s.jenis == "tanpa_nama") s.namaTanpaNama.trim().takeIf { it.isNotEmpty() } else null,
            beratKg = beratNorm,
            biayaUtama = s.biayaUtamaInput.toIntOrNull(),
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

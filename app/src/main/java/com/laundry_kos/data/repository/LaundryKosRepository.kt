package com.laundry_kos.data.repository

import android.os.Build
import com.laundry_kos.core.network.ApiEventBus
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.network.safeApiCall
import com.laundry_kos.core.session.SessionStore
import com.laundry_kos.data.api.LaundryKosApi
import com.laundry_kos.data.dto.*

class LaundryKosRepository(
    private val api: LaundryKosApi,
    val sessionStore: SessionStore,
    val eventBus: ApiEventBus,
) {
    @Volatile
    private var lastLocalMutationAtMs: Long = 0L

    fun wasRecentLocalMutation(windowMs: Long = 10_000L): Boolean =
        (System.currentTimeMillis() - lastLocalMutationAtMs) in 0L..windowMs

    private fun <T> trackMutation(result: ApiResult<T>): ApiResult<T> {
        if (result is ApiResult.Success) {
            lastLocalMutationAtMs = System.currentTimeMillis()
        }
        return result
    }
    // --- Auth & Session ---
    suspend fun login(username: String, password: String): ApiResult<AuthResponseDto> {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android" }
        val result = safeApiCall {
            api.login(
                LoginRequestDto(
                    username = username.trim(),
                    password = password,
                    deviceName = deviceName,
                ),
            )
        }
        if (result is ApiResult.Success) {
            sessionStore.saveSession(
                token = result.data.token,
                propertiId = result.data.propertiAktif?.id,
                userId = result.data.user.id,
            )
        }
        return result
    }

    suspend fun fetchMe(): ApiResult<MeResponseDto> {
        val result = safeApiCall(eventBus) { api.me() }
        if (result is ApiResult.Success) {
            sessionStore.savePropertiId(result.data.propertiAktif?.id)
            sessionStore.saveUserId(result.data.user.id)
        }
        return result
    }

    suspend fun selectProperti(propertiId: Int): ApiResult<MeResponseDto> {
        sessionStore.savePropertiId(propertiId)
        val result = safeApiCall(eventBus) { api.me() }
        if (result is ApiResult.Success) {
            sessionStore.saveUserId(result.data.user.id)
        }
        return result
    }

    suspend fun logout() {
        runCatching { api.unregisterDeviceToken() }
        runCatching { api.logout() }
        sessionStore.clearSession()
    }

    suspend fun registerDeviceToken(fcmToken: String) {
        val clean = fcmToken.trim()
        if (clean.length < 20) return
        sessionStore.saveFcmToken(clean)
        if (sessionStore.currentToken.isNullOrBlank()) return
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android" }
        runCatching {
            api.registerDeviceToken(
                DeviceTokenRequestDto(
                    token = clean,
                    platform = "android",
                    deviceName = deviceName,
                ),
            )
        }
    }

    suspend fun fetchProperti(): ApiResult<PropertiListResponseDto> =
        safeApiCall(eventBus) { api.properti() }

    suspend fun tambahProperti(request: SimpanPropertiRequestDto): ApiResult<PropertiMutasiResponseDto> =
        safeApiCall(eventBus) { api.tambahProperti(request) }

    suspend fun ubahProperti(id: Int, request: SimpanPropertiRequestDto): ApiResult<PropertiMutasiResponseDto> =
        safeApiCall(eventBus) { api.ubahProperti(id, request) }

    suspend fun fetchSetupProperti(id: Int): ApiResult<PropertiSetupResponseDto> =
        safeApiCall(eventBus) { api.setupProperti(id) }

    suspend fun setupKamar(id: Int, request: WizardKamarRequestDto): ApiResult<WizardKamarResponseDto> =
        safeApiCall(eventBus) { api.setupKamar(id, request) }

    suspend fun setupAturan(id: Int, request: SetupAturanDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.setupAturan(id, request) }

    suspend fun setupAktifkan(id: Int): ApiResult<PropertiMutasiResponseDto> =
        safeApiCall(eventBus) { api.setupAktifkan(id) }

    suspend fun fetchPenggunaList(): ApiResult<PenggunaListResponseDto> =
        safeApiCall(eventBus) { api.penggunaList() }

    suspend fun tambahPengguna(request: SimpanPenggunaRequestDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.tambahPengguna(request) }

    suspend fun toggleAktifPengguna(id: Int): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.toggleAktifPengguna(id) }

    suspend fun resetPasswordPengguna(id: Int, request: ResetPasswordRequestDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.resetPasswordPengguna(id, request) }

    suspend fun fetchKamar(hanyaKosong: Boolean = false): ApiResult<KamarListResponseDto> =
        safeApiCall(eventBus) { api.kamar(kosong = if (hanyaKosong) 1 else null) }

    // --- Beranda & Notifikasi ---
    suspend fun fetchBeranda(bulan: String? = null): ApiResult<BerandaResponseDto> =
        safeApiCall(eventBus) { api.beranda(bulan = bulan) }

    suspend fun fetchNotifikasi(): ApiResult<ServerNotifikasiResponseDto> =
        safeApiCall(eventBus) { api.notifikasi() }

    // --- Laundry ---
    suspend fun fetchLaundryList(
        periode: String = "bulan_ini",
        dari: String? = null,
        sampai: String? = null,
        cari: String? = null,
        jenis: String? = null,
        tampil: Int = 20,
    ): ApiResult<LaundryListResponseDto> = safeApiCall(eventBus) {
        api.laundryList(
            periode = periode,
            dari = dari?.takeIf { it.isNotBlank() },
            sampai = sampai?.takeIf { it.isNotBlank() },
            cari = cari?.takeIf { it.isNotBlank() },
            jenis = jenis?.takeIf { it.isNotBlank() },
            tampil = tampil,
        )
    }

    suspend fun fetchInfoKuota(
        tanggal: String,
        penghuniId: Int? = null,
        beratKg: String? = null,
        kecualiId: Int? = null,
    ): ApiResult<InfoKuotaResponseDto> = safeApiCall(eventBus) {
        api.infoKuota(
            tanggal = tanggal,
            penghuniId = penghuniId,
            beratKg = beratKg?.takeIf { it.isNotBlank() },
            kecualiId = kecualiId,
        )
    }

    suspend fun fetchLaundryDetail(id: Int): ApiResult<LaundryDetailResponseDto> =
        safeApiCall(eventBus) { api.detailLaundry(id) }

    suspend fun tambahLaundry(request: SimpanLaundryRequestDto): ApiResult<LaundryMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.tambahLaundry(request) })

    suspend fun ubahLaundry(id: Int, request: SimpanLaundryRequestDto): ApiResult<LaundryMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.ubahLaundry(id, request) })

    suspend fun hapusLaundry(id: Int): ApiResult<HapusLaundryResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.hapusLaundry(id) })

    suspend fun fetchLaundryTerhapus(tampil: Int = 15): ApiResult<TerhapusListResponseDto> =
        safeApiCall(eventBus) { api.laundryTerhapus(tampil = tampil) }

    suspend fun pulihkanLaundry(id: Int): ApiResult<LaundryMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.pulihkanLaundry(id) })

    suspend fun tetapkanPemilik(id: Int, penghuniId: Int): ApiResult<LaundryMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.tetapkanPemilik(id, TetapkanPemilikRequestDto(penghuniId)) })

    suspend fun lengkapiHarga(): ApiResult<LengkapiHargaResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.lengkapiHarga() })

    // --- Penghuni ---
    suspend fun fetchPenghuniList(
        tab: String = "semua",
        cari: String? = null,
        bulan: String? = null,
    ): ApiResult<PenghuniListResponseDto> = safeApiCall(eventBus) {
        api.penghuniList(
            tab = tab,
            cari = cari?.takeIf { it.isNotBlank() },
            bulan = bulan?.takeIf { it.isNotBlank() },
        )
    }

    suspend fun fetchPenghuniDetail(id: Int, bulan: String? = null): ApiResult<PenghuniDetailResponseDto> =
        safeApiCall(eventBus) { api.detailPenghuni(id, bulan) }

    suspend fun tambahPenghuni(request: MasukPenghuniRequestDto): ApiResult<PenghuniMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.tambahPenghuni(request) })

    suspend fun ubahPenghuni(id: Int, request: UbahPenghuniRequestDto): ApiResult<PenghuniMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.ubahPenghuni(id, request) })

    suspend fun pindahKamar(id: Int, request: PindahKamarRequestDto): ApiResult<PenempatanMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.pindahKamar(id, request) })

    suspend fun batalkanPindah(id: Int): ApiResult<PenempatanMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.batalkanPindah(id) })

    suspend fun keluarPenghuni(id: Int, request: KeluarPenghuniRequestDto): ApiResult<PenghuniMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.keluarPenghuni(id, request) })

    suspend fun batalkanKeluar(id: Int): ApiResult<PenghuniMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.batalkanKeluar(id) })

    suspend fun gantiNomor(id: Int, noWa: String?): ApiResult<PenghuniMutasiResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.gantiNomor(id, GantiNomorRequestDto(noWa?.takeIf { it.isNotBlank() })) })

    suspend fun simpanPenyesuaianKuota(
        penghuniId: Int,
        request: PenyesuaianKuotaRequestDto,
    ): ApiResult<PenyesuaianKuotaResponseDto> =
        trackMutation(safeApiCall(eventBus) { api.simpanPenyesuaianKuota(penghuniId, request) })

    // --- Laporan & WA ---
    suspend fun fetchLaporan(
        periode: String = "bulan_ini",
        dari: String? = null,
        sampai: String? = null,
        penghuniId: Int? = null,
        rinci: Boolean = false,
    ): ApiResult<LaporanResponse> = safeApiCall(eventBus) {
        api.laporan(
            periode = periode,
            dari = dari?.takeIf { it.isNotBlank() },
            sampai = sampai?.takeIf { it.isNotBlank() },
            penghuniId = penghuniId,
            rinci = if (rinci) 1 else 0,
        )
    }

    suspend fun fetchWaTagihan(penghuniId: Int, bulan: String? = null): ApiResult<WaMessageDto> =
        safeApiCall(eventBus) { api.waTagihan(penghuniId, bulan) }

    suspend fun fetchWaPeringatan(penghuniId: Int, bulan: String? = null): ApiResult<WaMessageDto> =
        safeApiCall(eventBus) { api.waPeringatan(penghuniId, bulan) }

    // --- Pengaturan ---
    suspend fun fetchPengaturan(tanggal: String? = null): ApiResult<PengaturanResponseDto> =
        safeApiCall(eventBus) { api.pengaturan(tanggal = tanggal?.takeIf { it.isNotBlank() }) }

    suspend fun simpanAturan(request: SimpanAturanRequestDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.simpanAturan(request) }

    suspend fun simpanIzinPenjaga(request: SimpanIzinPenjagaRequestDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.simpanIzinPenjaga(request) }

    suspend fun simpanTemplateWa(request: SimpanTemplateWaRequestDto): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.simpanTemplateWa(request) }

    suspend fun resetTemplateWa(kunci: String? = null): ApiResult<MessageResponseDto> =
        safeApiCall(eventBus) { api.resetTemplateWa(ResetTemplateWaRequestDto(kunci)) }
}

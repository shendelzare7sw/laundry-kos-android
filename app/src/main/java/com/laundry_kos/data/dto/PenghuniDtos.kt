package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PenghuniDto(
    val id: Int,
    @SerialName("properti_id") val propertiId: Int = 0,
    val nama: String,
    val alias: String? = null,
    @SerialName("no_wa") val noWa: String? = null,
    val status: String = "aktif",
    @SerialName("status_label") val statusLabel: String = "",
    @SerialName("kamar_id") val kamarId: Int? = null,
    @SerialName("kamar_nomor") val kamarNomor: String? = null,
    @SerialName("tanggal_masuk") val tanggalMasuk: String? = null,
    @SerialName("tanggal_masuk_perkiraan") val tanggalMasukPerkiraan: Boolean = false,
    @SerialName("jangka_waktu_sewa") val jangkaWaktuSewa: String = "tidak_tentu",
    @SerialName("jangka_waktu_label") val jangkaWaktuLabel: String = "Tidak Tentu",
    @SerialName("tanggal_keluar_rencana") val tanggalKeluarRencana: String? = null,
    @SerialName("tanggal_keluar") val tanggalKeluar: String? = null,
    @SerialName("alasan_keluar") val alasanKeluar: String? = null,
    val catatan: String? = null,
    val kuota: KuotaStatusDto? = null,
)

@Serializable
data class PenempatanDto(
    val id: Int,
    @SerialName("kamar_id") val kamarId: Int = 0,
    @SerialName("kamar_nomor") val kamarNomor: String? = null,
    @SerialName("kamar_lantai") val kamarLantai: Int? = null,
    val mulai: String? = null,
    val selesai: String? = null,
    val sebab: String? = null,
    val aktif: Boolean = false,
    @SerialName("dibuat_oleh_nama") val dibuatOlehNama: String? = null,
)

@Serializable
data class PenghuniHitunganDto(
    val semua: Int = 0,
    val booking: Int = 0,
    val aktif: Int = 0,
    val keluar: Int = 0,
)

@Serializable
data class PenghuniListResponseDto(
    val tab: String = "semua",
    @SerialName("rentang_bulan") val rentangBulan: RentangBulanDto = RentangBulanDto(bulan = ""),
    val hitungan: PenghuniHitunganDto = PenghuniHitunganDto(),
    @SerialName("boleh_kelola_kontrak") val bolehKelolaKontrak: Boolean = false,
    val data: List<PenghuniDto> = emptyList(),
)

@Serializable
data class PenghuniPermissionsDto(
    @SerialName("boleh_kelola_kontrak") val bolehKelolaKontrak: Boolean = false,
    @SerialName("boleh_kelola_penghuni") val bolehKelolaPenghuni: Boolean = false,
    @SerialName("boleh_sesuaikan_kuota") val bolehSesuaikanKuota: Boolean = false,
    @SerialName("bisa_batalkan_pindah") val bisaBatalkanPindah: Boolean = false,
)

@Serializable
data class PenghuniDetailResponseDto(
    val penghuni: PenghuniDto,
    val penempatan: List<PenempatanDto> = emptyList(),
    @SerialName("kuota_bulan_ini") val kuotaBulanIni: KuotaStatusDto,
    @SerialName("riwayat_kuota") val riwayatKuota: List<KuotaStatusDto> = emptyList(),
    @SerialName("catatan_laundry") val catatanLaundry: List<LaundryDto> = emptyList(),
    @SerialName("wa_tagihan") val waTagihan: WaMessageDto? = null,
    @SerialName("wa_peringatan") val waPeringatan: WaMessageDto? = null,
    val permissions: PenghuniPermissionsDto = PenghuniPermissionsDto(),
)

@Serializable
data class MasukPenghuniRequestDto(
    val nama: String,
    val alias: String? = null,
    @SerialName("no_wa") val noWa: String? = null,
    val status: String = "aktif",
    @SerialName("kamar_id") val kamarId: Int,
    @SerialName("tanggal_masuk") val tanggalMasuk: String,
    @SerialName("tanggal_masuk_perkiraan") val tanggalMasukPerkiraan: Boolean = false,
    @SerialName("jangka_waktu_sewa") val jangkaWaktuSewa: String = "tidak_tentu",
    @SerialName("tanggal_keluar_rencana") val tanggalKeluarRencana: String? = null,
    val catatan: String? = null,
)

@Serializable
data class UbahPenghuniRequestDto(
    val nama: String,
    val alias: String? = null,
    val status: String? = null,
    @SerialName("tanggal_masuk") val tanggalMasuk: String? = null,
    @SerialName("tanggal_masuk_perkiraan") val tanggalMasukPerkiraan: Boolean = false,
    @SerialName("jangka_waktu_sewa") val jangkaWaktuSewa: String? = null,
    @SerialName("tanggal_keluar_rencana") val tanggalKeluarRencana: String? = null,
    val catatan: String? = null,
)

@Serializable
data class PindahKamarRequestDto(
    @SerialName("kamar_tujuan_id") val kamarTujuanId: Int,
    @SerialName("tanggal_pindah") val tanggalPindah: String,
)

@Serializable
data class KeluarPenghuniRequestDto(
    @SerialName("tanggal_keluar") val tanggalKeluar: String,
    @SerialName("alasan_keluar") val alasanKeluar: String? = null,
)

@Serializable
data class GantiNomorRequestDto(
    @SerialName("no_wa") val noWa: String?,
)

@Serializable
data class PenyesuaianKuotaRequestDto(
    val bulan: String,
    val nominal: Int? = null,
    val reset: Boolean = false,
    val catatan: String? = null,
)

@Serializable
data class PenghuniMutasiResponseDto(
    val message: String = "",
    val peringatan: String? = null,
    val data: PenghuniDto? = null,
)

@Serializable
data class PenempatanMutasiResponseDto(
    val message: String = "",
    val penempatan: PenempatanDto? = null,
)

@Serializable
data class PenyesuaianKuotaResponseDto(
    val message: String = "",
    val kuota: KuotaStatusDto,
)

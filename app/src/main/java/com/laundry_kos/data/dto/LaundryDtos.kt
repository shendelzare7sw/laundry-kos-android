package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LaundryItemDto(
    val id: Int = 0,
    val nama: String = "",
    val jumlah: Int = 1,
    @SerialName("berat_centi") val beratCenti: Int? = null,
    @SerialName("berat_label") val beratLabel: String? = null,
    val biaya: Int = 0,
    val urutan: Int = 0,
)

@Serializable
data class LaundryDto(
    val id: Int,
    @SerialName("properti_id") val propertiId: Int = 0,
    val tanggal: String = "",
    val jenis: String = "penghuni",
    @SerialName("jenis_label") val jenisLabel: String = "",
    @SerialName("penghuni_id") val penghuniId: Int? = null,
    @SerialName("penghuni_nama") val penghuniNama: String? = null,
    @SerialName("kamar_id") val kamarId: Int? = null,
    @SerialName("kamar_nomor") val kamarNomor: String? = null,
    @SerialName("berat_centi") val beratCenti: Int? = null,
    @SerialName("berat_label") val beratLabel: String? = null,
    @SerialName("biaya_utama") val biayaUtama: Int? = null,
    @SerialName("biaya_manual") val biayaManual: Boolean = false,
    @SerialName("item_biaya") val itemBiaya: Int = 0,
    val item: List<LaundryItemDto> = emptyList(),
    val catatan: String? = null,
    val ref: String? = null,
    @SerialName("dicatat_oleh") val dicatatOleh: Int? = null,
    @SerialName("dicatat_oleh_nama") val dicatatOlehNama: String? = null,
    @SerialName("lewat_kuota") val lewatKuota: Boolean = false,
    @SerialName("kuota_bulan") val kuotaBulan: KuotaStatusDto? = null,
    @SerialName("boleh_ubah") val bolehUbah: Boolean = false,
    @SerialName("boleh_hapus") val bolehHapus: Boolean = false,
    @SerialName("boleh_tetapkan_pemilik") val bolehTetapkanPemilik: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
data class LaundryRentangDto(
    val periode: String = "bulan_ini",
    val dari: String? = null,
    val sampai: String? = null,
    val label: String = "",
)

@Serializable
data class LaundryRingkasanDto(
    @SerialName("total_berat_centi") val totalBeratCenti: Int = 0,
    @SerialName("total_berat_kg") val totalBeratKg: String = "0",
    @SerialName("kuota_gratis_centi") val kuotaGratisCenti: Int = 0,
    @SerialName("kuota_gratis_kg") val kuotaGratisKg: String = "0",
    @SerialName("total_biaya_utama") val totalBiayaUtama: Int = 0,
    @SerialName("total_bayar_terpisah") val totalBayarTerpisah: Int = 0,
    @SerialName("jumlah_catatan") val jumlahCatatan: Int = 0,
    @SerialName("jumlah_catatan_terpisah") val jumlahCatatanTerpisah: Int = 0,
    @SerialName("jumlah_item_terpisah") val jumlahItemTerpisah: Int = 0,
    @SerialName("jumlah_tanpa_nama") val jumlahTanpaNama: Int = 0,
)

@Serializable
data class TampilMetaDto(
    val ditampilkan: Int = 0,
    val total: Int = 0,
    val langkah: Int = 20,
    @SerialName("ada_lagi") val adaLagi: Boolean = false,
    @SerialName("tampil_berikutnya") val tampilBerikutnya: Int = 20,
)

@Serializable
data class PratinjauLengkapiHargaDto(
    val jumlah: Int = 0,
    val total: Int = 0,
)

@Serializable
data class LaundryListResponseDto(
    val rentang: LaundryRentangDto = LaundryRentangDto(),
    val ringkasan: LaundryRingkasanDto = LaundryRingkasanDto(),
    val meta: TampilMetaDto = TampilMetaDto(),
    @SerialName("pratinjau_lengkapi_harga") val pratinjauLengkapiHarga: PratinjauLengkapiHargaDto = PratinjauLengkapiHargaDto(),
    @SerialName("jumlah_terhapus") val jumlahTerhapus: Int = 0,
    val data: List<LaundryDto> = emptyList(),
)

@Serializable
data class InfoKuotaPenghuniDto(
    val id: Int,
    val nama: String,
    val alias: String? = null,
    val kamar: String = "-",
    val status: String = "aktif",
    val label: String = "",
)

@Serializable
data class InfoKuotaPreviewDto(
    @SerialName("mode_kuota") val modeKuota: String = "penghuni",
    @SerialName("label_bulan") val labelBulan: String = "",
    @SerialName("kuota_kg") val kuotaKg: String = "0",
    @SerialName("sebelum_kg") val sebelumKg: String = "0",
    @SerialName("sesudah_kg") val sesudahKg: String = "0",
    @SerialName("sisa_kg") val sisaKg: String = "0",
    @SerialName("lewat_kg") val lewatKg: String = "0",
    val status: String = "normal",
)

@Serializable
data class DuplikatLaundryDto(
    val id: Int,
    @SerialName("url_ubah") val urlUbah: String? = null,
)

@Serializable
data class InfoKuotaResponseDto(
    @SerialName("tarif_per_kg") val tarifPerKg: Int = 8000,
    @SerialName("min_kg") val minKg: String = "2",
    @SerialName("min_kg_centi") val minKgCenti: Int = 200,
    @SerialName("biaya_otomatis") val biayaOtomatis: Int? = null,
    val kamar: String? = null,
    val kuota: InfoKuotaPreviewDto? = null,
    val galat: String? = null,
    val duplikat: DuplikatLaundryDto? = null,
    @SerialName("daftar_penghuni") val daftarPenghuni: List<InfoKuotaPenghuniDto> = emptyList(),
)

@Serializable
data class SimpanLaundryItemDto(
    val nama: String,
    val jumlah: Int = 1,
    @SerialName("berat_kg") val beratKg: String? = null,
    val biaya: Int,
)

@Serializable
data class SimpanLaundryRequestDto(
    val jenis: String,
    val tanggal: String,
    @SerialName("penghuni_id") val penghuniId: Int? = null,
    @SerialName("berat_kg") val beratKg: String? = null,
    @SerialName("biaya_utama") val biayaUtama: Int? = null,
    @SerialName("konfirmasi_berat_besar") val konfirmasiBeratBesar: Boolean = false,
    val catatan: String? = null,
    val ref: String? = null,
    val item: List<SimpanLaundryItemDto> = emptyList(),
)

@Serializable
data class LaundryDetailResponseDto(
    val data: LaundryDto,
)

@Serializable
data class LaundryMutasiResponseDto(
    val message: String = "",
    val data: LaundryDto,
)

@Serializable
data class HapusLaundryResponseDto(
    val message: String = "",
    @SerialName("boleh_pulihkan") val bolehPulihkan: Boolean = false,
)

@Serializable
data class TerhapusListResponseDto(
    val meta: TampilMetaDto = TampilMetaDto(),
    val data: List<LaundryDto> = emptyList(),
)

@Serializable
data class TetapkanPemilikRequestDto(
    @SerialName("penghuni_id") val penghuniId: Int,
)

@Serializable
data class LengkapiHargaResponseDto(
    @SerialName("jumlah_diperbarui") val jumlahDiperbarui: Int = 0,
    val message: String = "",
)

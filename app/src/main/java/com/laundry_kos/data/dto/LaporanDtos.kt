package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LaporanRentangDto(
    val periode: String = "bulan_ini",
    val dari: String = "",
    val sampai: String = "",
    val label: String = "",
    @SerialName("bulan_acuan") val bulanAcuan: String = "",
    @SerialName("is_bulan_penuh") val isBulanPenuh: Boolean = true,
)

@Serializable
data class LaporanFilterDto(
    val periode: String = "bulan_ini",
    val dari: String? = null,
    val sampai: String? = null,
    @SerialName("penghuni_id") val penghuniId: Int? = null,
    val rinci: Boolean = false,
)

@Serializable
data class LaporanRingkasanDto(
    @SerialName("total_kg_centi") val totalKgCenti: Int = 0,
    @SerialName("total_kg_label") val totalKgLabel: String = "0",
    @SerialName("total_catatan") val totalCatatan: Int = 0,
    @SerialName("jumlah_penghuni") val jumlahPenghuni: Int = 0,
    @SerialName("jumlah_mendekati_lewat") val jumlahMendekatiLewat: Int = 0,
    @SerialName("total_biaya_utama") val totalBiayaUtama: Int = 0,
    @SerialName("total_tagihan_lewat") val totalTagihanLewat: Int = 0,
    @SerialName("total_bayar_terpisah") val totalBayarTerpisah: Int = 0,
)

@Serializable
data class LaporanPenghuniRingkasDto(
    val id: Int,
    val nama: String,
    val alias: String? = null,
    @SerialName("no_wa") val noWa: String? = null,
    val status: String = "aktif",
)

@Serializable
data class LaporanBarisDto(
    val tipe: String = "utama",
    @SerialName("laundry_id") val laundryId: Int = 0,
    val tanggal: String = "",
    @SerialName("tanggal_label") val tanggalLabel: String = "",
    @SerialName("berat_centi") val beratCenti: Int? = null,
    @SerialName("berat_label") val beratLabel: String? = null,
    @SerialName("nama_item") val namaItem: String? = null,
    val biaya: Int? = null,
    val merah: Boolean = false,
    val catatan: String? = null,
)

@Serializable
data class LaporanKartuPenghuniDto(
    val penghuni: LaporanPenghuniRingkasDto,
    @SerialName("label_kamar") val labelKamar: String = "-",
    @SerialName("jumlah_catatan") val jumlahCatatan: Int = 0,
    @SerialName("total_kg_centi") val totalKgCenti: Int = 0,
    @SerialName("total_kg_label") val totalKgLabel: String = "0",
    @SerialName("biaya_utama") val biayaUtama: Int = 0,
    @SerialName("bayar_terpisah") val bayarTerpisah: Int = 0,
    @SerialName("jumlah_item_terpisah") val jumlahItemTerpisah: Int = 0,
    @SerialName("tagihan_lewat_kuota") val tagihanLewatKuota: Int = 0,
    val kuota: KuotaStatusDto,
    @SerialName("tampilkan_periode_ini") val tampilkanPeriodeIni: Boolean = false,
    @SerialName("boleh_sesuaikan_kuota") val bolehSesuaikanKuota: Boolean = false,
    val baris: List<LaporanBarisDto> = emptyList(),
    @SerialName("wa_tagihan") val waTagihan: WaMessageDto? = null,
)

@Serializable
data class LaporanKartuLainnyaDto(
    val jenis: String,
    val judul: String,
    @SerialName("jumlah_catatan") val jumlahCatatan: Int = 0,
    @SerialName("total_kg_centi") val totalKgCenti: Int = 0,
    @SerialName("total_kg_label") val totalKgLabel: String = "0",
    @SerialName("biaya_utama") val biayaUtama: Int = 0,
    @SerialName("bayar_terpisah") val bayarTerpisah: Int = 0,
    @SerialName("jumlah_item_terpisah") val jumlahItemTerpisah: Int = 0,
    val baris: List<LaporanBarisDto> = emptyList(),
)

@Serializable
data class LaporanResponse(
    @SerialName("nama_properti") val namaProperti: String = "",
    val rentang: LaporanRentangDto = LaporanRentangDto(),
    val filter: LaporanFilterDto = LaporanFilterDto(),
    @SerialName("dicetak_label") val dicetakLabel: String = "",
    @SerialName("kuota_gratis_centi") val kuotaGratisCenti: Int = 2000,
    @SerialName("kuota_gratis_label") val kuotaGratisLabel: String = "20,0",
    val ringkasan: LaporanRingkasanDto = LaporanRingkasanDto(),
    @SerialName("kartu_penghuni") val kartuPenghuni: List<LaporanKartuPenghuniDto> = emptyList(),
    @SerialName("kartu_lainnya") val kartuLainnya: List<LaporanKartuLainnyaDto> = emptyList(),
    @SerialName("rekap_wa") val rekapWa: RekapWaDto = RekapWaDto(),
)

package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RentangBulanDto(
    val bulan: String,
    val awal: String = "",
    val akhir: String = "",
    val label: String = "",
)

@Serializable
data class KuotaStatusDto(
    @SerialName("penghuni_id") val penghuniId: Int = 0,
    val bulan: String = "",
    @SerialName("label_bulan") val labelBulan: String = "",
    @SerialName("mode_kuota") val modeKuota: String = "penghuni",
    @SerialName("terpakai_centi") val terpakaiCenti: Int = 0,
    @SerialName("terpakai_kg") val terpakaiKg: String = "0",
    @SerialName("terpakai_label") val terpakaiLabel: String? = null,
    @SerialName("kuota_centi") val kuotaCenti: Int = 0,
    @SerialName("kuota_kg") val kuotaKg: String = "0",
    @SerialName("kuota_label") val kuotaLabel: String? = null,
    @SerialName("sisa_centi") val sisaCenti: Int = 0,
    @SerialName("sisa_kg") val sisaKg: String = "0",
    @SerialName("kelebihan_centi") val kelebihanCenti: Int = 0,
    @SerialName("kelebihan_kg") val kelebihanKg: String = "0",
    @SerialName("kelebihan_label") val kelebihanLabel: String? = null,
    val persen: Int = 0,
    @SerialName("persen_bar") val persenBar: Int = 0,
    @SerialName("ambang_mendekati_persen") val ambangMendekatiPersen: Int = 75,
    @SerialName("toleransi_centi") val toleransiCenti: Int = 0,
    @SerialName("toleransi_kg") val toleransiKg: String = "0",
    @SerialName("tarif_per_kg") val tarifPerKg: Int = 8000,
    val pembulatan: Int = 1000,
    val status: String = "normal",
    @SerialName("di_bawah_toleransi") val diBawahToleransi: Boolean = false,
    @SerialName("jumlah_catatan") val jumlahCatatan: Int = 0,
    @SerialName("total_biaya_utama") val totalBiayaUtama: Int = 0,
    @SerialName("bayar_terpisah") val bayarTerpisah: Int = 0,
    @SerialName("tagihan_otomatis") val tagihanOtomatis: Int = 0,
    @SerialName("nominal_penyesuaian") val nominalPenyesuaian: Int? = null,
    @SerialName("catatan_penyesuaian") val catatanPenyesuaian: String? = null,
    @SerialName("tagihan_lewat_kuota") val tagihanLewatKuota: Int = 0,
    @SerialName("total_tagihan") val totalTagihan: Int = 0,
)

@Serializable
data class WaMessageDto(
    val pesan: String = "",
    val nomor: String? = null,
    val url: String? = null,
    @SerialName("total_tagihan") val totalTagihan: Int? = null,
)

@Serializable
data class RekapWaDto(
    val pesan: String = "",
    val url: String? = null,
    @SerialName("terlalu_panjang") val terlaluPanjang: Boolean = false,
    @SerialName("jumlah_karakter") val jumlahKarakter: Int = 0,
)

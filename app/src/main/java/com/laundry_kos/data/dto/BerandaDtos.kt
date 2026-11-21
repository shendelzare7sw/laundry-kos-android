package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BerandaResponseDto(
    val properti: PropertiDto,
    @SerialName("rentang_bulan") val rentangBulan: RentangBulanDto,
    val ringkasan: BerandaRingkasanDto,
    @SerialName("daftar_peringatan") val daftarPeringatan: List<PeringatanKuotaItemDto> = emptyList(),
)

@Serializable
data class BerandaRingkasanDto(
    @SerialName("total_berat_centi") val totalBeratCenti: Int = 0,
    @SerialName("total_berat_label") val totalBeratLabel: String = "0",
    @SerialName("jumlah_penghuni_mencatat") val jumlahPenghuniMencatat: Int = 0,
    @SerialName("total_biaya_utama") val totalBiayaUtama: Int = 0,
    @SerialName("total_bayar_terpisah") val totalBayarTerpisah: Int = 0,
)

@Serializable
data class PeringatanKuotaItemDto(
    @SerialName("penghuni_id") val penghuniId: Int,
    val nama: String,
    @SerialName("kamar_nomor") val kamarNomor: String = "-",
    @SerialName("no_wa") val noWa: String? = null,
    val kuota: KuotaStatusDto,
    @SerialName("wa_url") val waUrl: String? = null,
)

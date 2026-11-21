package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AturanDto(
    @SerialName("nama_properti") val namaProperti: String = "",
    @SerialName("tarif_per_kg") val tarifPerKg: Int = 8000,
    @SerialName("min_kg") val minKg: String = "2",
    @SerialName("min_kg_centi") val minKgCenti: Int = 200,
    @SerialName("kuota_kg") val kuotaKg: String = "20",
    @SerialName("kuota_kg_centi") val kuotaKgCenti: Int = 2000,
    @SerialName("ambang_mendekati_persen") val ambangMendekatiPersen: Int = 75,
    @SerialName("toleransi_kg") val toleransiKg: String = "1",
    @SerialName("toleransi_kg_centi") val toleransiKgCenti: Int = 100,
    val pembulatan: Int = 1000,
    @SerialName("mode_kuota") val modeKuota: String = "penghuni",
    @SerialName("kuota_prorata_bulan_masuk") val kuotaProrataBulanMasuk: Boolean = false,
)

@Serializable
data class IzinPenjagaDto(
    @SerialName("jam_edit_penjaga") val jamEditPenjaga: Int? = 24,
    @SerialName("penjaga_kelola_kontrak") val penjagaKelolaKontrak: Boolean = false,
    @SerialName("penjaga_ubah_catatan_lain") val penjagaUbahCatatanLain: Boolean = false,
)

@Serializable
data class TemplateWaDto(
    @SerialName("template_wa_tagihan") val templateWaTagihan: String = "",
    @SerialName("template_wa_peringatan") val templateWaPeringatan: String = "",
    @SerialName("template_wa_rekap") val templateWaRekap: String = "",
)

@Serializable
data class PengaturanResponseDto(
    @SerialName("tanggal_acuan") val tanggalAcuan: String = "",
    @SerialName("boleh_ubah") val bolehUbah: Boolean = false,
    val aturan: AturanDto = AturanDto(),
    @SerialName("izin_penjaga") val izinPenjaga: IzinPenjagaDto = IzinPenjagaDto(),
    @SerialName("template_wa") val templateWa: TemplateWaDto = TemplateWaDto(),
)

@Serializable
data class SimpanAturanRequestDto(
    @SerialName("berlaku_mulai") val berlakuMulai: String,
    @SerialName("nama_properti") val namaProperti: String,
    @SerialName("tarif_per_kg") val tarifPerKg: Int,
    @SerialName("min_kg") val minKg: String,
    @SerialName("kuota_kg") val kuotaKg: String,
    @SerialName("ambang_mendekati_persen") val ambangMendekatiPersen: Int,
    @SerialName("toleransi_kg") val toleransiKg: String,
    val pembulatan: Int,
    @SerialName("mode_kuota") val modeKuota: String,
    @SerialName("kuota_prorata_bulan_masuk") val kuotaProrataBulanMasuk: Boolean,
)

@Serializable
data class SimpanIzinPenjagaRequestDto(
    @SerialName("jam_edit_penjaga") val jamEditPenjaga: Int? = null,
    @SerialName("penjaga_kelola_kontrak") val penjagaKelolaKontrak: Boolean,
    @SerialName("penjaga_ubah_catatan_lain") val penjagaUbahCatatanLain: Boolean,
)

@Serializable
data class SimpanTemplateWaRequestDto(
    @SerialName("template_wa_tagihan") val templateWaTagihan: String,
    @SerialName("template_wa_peringatan") val templateWaPeringatan: String,
    @SerialName("template_wa_rekap") val templateWaRekap: String,
)

@Serializable
data class ResetTemplateWaRequestDto(
    val kunci: String? = null,
)

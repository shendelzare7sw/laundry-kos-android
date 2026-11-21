package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimpanPropertiRequestDto(
    val nama: String,
    @SerialName("nama_properti_laporan") val namaPropertiLaporan: String,
    val alamat: String? = null,
    @SerialName("no_wa_admin") val noWaAdmin: String? = null,
    val deskripsi: String? = null,
    val status: String = "draf",
)

@Serializable
data class PropertiMutasiResponseDto(
    val message: String = "",
    val data: PropertiDto,
)

@Serializable
data class KamarLantaiGroupDto(
    val lantai: Int,
    val jumlah: Int = 0,
    val nomor: List<String> = emptyList(),
)

@Serializable
data class SetupAturanDto(
    @SerialName("tarif_per_kg") val tarifPerKg: Int = 8000,
    @SerialName("min_kg") val minKg: String = "2",
    @SerialName("kuota_kg") val kuotaKg: String = "20",
    @SerialName("ambang_mendekati_persen") val ambangMendekatiPersen: Int = 75,
    @SerialName("toleransi_kg") val toleransiKg: String = "1",
    val pembulatan: Int = 1000,
    @SerialName("mode_kuota") val modeKuota: String = "penghuni",
    @SerialName("jam_edit_penjaga") val jamEditPenjaga: Int = 24,
    @SerialName("penjaga_kelola_kontrak") val penjagaKelolaKontrak: Boolean = false,
)

@Serializable
data class PropertiSetupResponseDto(
    val properti: PropertiDto,
    @SerialName("siap_kamar") val siapKamar: Boolean = false,
    @SerialName("siap_aturan") val siapAturan: Boolean = false,
    @SerialName("siap_admin") val siapAdmin: Boolean = false,
    @SerialName("kamar_per_lantai") val kamarPerLantai: List<KamarLantaiGroupDto> = emptyList(),
    val aturan: SetupAturanDto = SetupAturanDto(),
    @SerialName("admin_aktif") val adminAktif: UserDto? = null,
    val penjaga: List<UserDto> = emptyList(),
)

@Serializable
data class WizardKamarRangeDto(
    val lantai: Int,
    val dari: Int,
    val sampai: Int,
)

@Serializable
data class WizardKamarRequestDto(
    val lantai: List<WizardKamarRangeDto>,
)

@Serializable
data class WizardKamarResponseDto(
    @SerialName("jumlah_dibuat") val jumlahDibuat: Int = 0,
    val message: String = "",
)

@Serializable
data class PenggunaItemDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String? = null,
    val role: String,
    @SerialName("role_label") val roleLabel: String = "",
    @SerialName("properti_id") val propertiId: Int? = null,
    @SerialName("properti_nama") val propertiNama: String? = null,
    val aktif: Boolean = true,
    @SerialName("boleh_ubah") val bolehUbah: Boolean = false,
)

@Serializable
data class PeranOptionDto(
    val value: String,
    val label: String,
)

@Serializable
data class PenggunaListResponseDto(
    val data: List<PenggunaItemDto> = emptyList(),
    @SerialName("peran_diizinkan") val peranDiizinkan: List<PeranOptionDto> = emptyList(),
    @SerialName("properti_pilihan") val propertiPilihan: List<PropertiDto> = emptyList(),
)

@Serializable
data class SimpanPenggunaRequestDto(
    val name: String,
    val username: String,
    val email: String? = null,
    val password: String,
    val role: String,
    @SerialName("properti_id") val propertiId: Int? = null,
)

@Serializable
data class ResetPasswordRequestDto(
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String,
)

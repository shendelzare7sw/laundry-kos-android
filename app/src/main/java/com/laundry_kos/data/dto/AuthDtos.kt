package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
    @SerialName("device_name") val deviceName: String,
)

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String? = null,
    val role: String,
    @SerialName("role_label") val roleLabel: String = "",
    @SerialName("properti_id") val propertiId: Int? = null,
    val aktif: Boolean = true,
)

@Serializable
data class PropertiDto(
    val id: Int,
    val nama: String,
    @SerialName("nama_properti_laporan") val namaPropertiLaporan: String? = null,
    val alamat: String? = null,
    val status: String = "aktif",
    @SerialName("jumlah_kamar") val jumlahKamar: Int? = null,
    @SerialName("jumlah_penghuni_aktif") val jumlahPenghuniAktif: Int? = null,
)

@Serializable
data class PermissionsDto(
    @SerialName("kelola_properti") val kelolaProperti: Boolean = false,
    @SerialName("kelola_pengguna") val kelolaPengguna: Boolean = false,
    @SerialName("kelola_pengaturan") val kelolaPengaturan: Boolean = false,
    @SerialName("kelola_kontrak") val kelolaKontrak: Boolean = false,
    @SerialName("catat_laundry") val catatLaundry: Boolean = false,
    @SerialName("lengkapi_harga") val lengkapiHarga: Boolean = false,
    @SerialName("lihat_terhapus") val lihatTerhapus: Boolean = false,
    @SerialName("sesuaikan_kuota") val sesuaikanKuota: Boolean = false,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    val user: UserDto,
    @SerialName("properti_aktif") val propertiAktif: PropertiDto? = null,
    @SerialName("properti_list") val propertiList: List<PropertiDto> = emptyList(),
    val permissions: PermissionsDto = PermissionsDto(),
)

@Serializable
data class MeResponseDto(
    val user: UserDto,
    @SerialName("properti_aktif") val propertiAktif: PropertiDto? = null,
    @SerialName("properti_list") val propertiList: List<PropertiDto> = emptyList(),
    val permissions: PermissionsDto = PermissionsDto(),
)

@Serializable
data class PropertiListResponseDto(
    val data: List<PropertiDto> = emptyList(),
)

@Serializable
data class KamarDto(
    val id: Int,
    val nomor: String,
    val lantai: Int = 1,
    val aktif: Boolean = true,
    val terisi: Boolean = false,
    @SerialName("penghuni_id") val penghuniId: Int? = null,
    @SerialName("penghuni_nama") val penghuniNama: String? = null,
    @SerialName("penghuni_status") val penghuniStatus: String? = null,
)

@Serializable
data class KamarListResponseDto(
    val data: List<KamarDto> = emptyList(),
)

@Serializable
data class MessageResponseDto(
    val message: String = "",
)

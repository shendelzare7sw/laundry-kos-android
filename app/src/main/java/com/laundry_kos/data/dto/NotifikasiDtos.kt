package com.laundry_kos.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerNotifikasiItemDto(
    val id: String,
    val tipe: String = "activity",
    val judul: String,
    val keterangan: String = "",
    @SerialName("waktu_label") val waktuLabel: String? = null,
    @SerialName("timestamp_ms") val timestampMs: Long? = null,
    @SerialName("aktor_id") val aktorId: Int? = null,
    @SerialName("penghuni_id") val penghuniId: Int? = null,
    @SerialName("laundry_id") val laundryId: Int? = null,
)

@Serializable
data class ServerNotifikasiResponseDto(
    val items: List<ServerNotifikasiItemDto> = emptyList(),
    @SerialName("server_time_ms") val serverTimeMs: Long? = null,
)

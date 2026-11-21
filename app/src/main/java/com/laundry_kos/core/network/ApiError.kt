package com.laundry_kos.core.network

import com.laundry_kos.data.dto.PropertiDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Serializable
data class ApiErrorPayload(
    val message: String? = null,
    val code: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    val errors: Map<String, List<String>>? = null,
    @SerialName("properti_list") val propertiList: List<PropertiDto>? = null,
)

data class ApiError(
    val httpCode: Int? = null,
    val code: String? = null,
    val message: String,
    val fieldErrors: Map<String, List<String>> = emptyMap(),
    val propertiList: List<PropertiDto> = emptyList(),
    val requestId: String? = null,
) {
    val isUnauthorized: Boolean
        get() = httpCode == 401 || code == "UNAUTHORIZED"

    val isPropertiBelumDipilih: Boolean
        get() = httpCode == 422 && code == "PROPERTI_BELUM_DIPILIH"

    val isAccountOrPropertyBlocked: Boolean
        get() = httpCode == 403 && code in setOf(
            "AKUN_NONAKTIF",
            "PROPERTI_TIDAK_AKTIF",
            "PROPERTI_TIDAK_TERIKAT",
        )

    val isForbiddenBlocked: Boolean
        get() = isAccountOrPropertyBlocked || (httpCode == 403 && code == "AKSES_PROPERTI_DITOLAK")

    fun firstFieldError(field: String): String? = fieldErrors[field]?.firstOrNull()

    fun summaryMessage(): String {
        val firstValidation = fieldErrors.values.firstOrNull()?.firstOrNull()
        return firstValidation ?: message
    }
}

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

object ApiErrorParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun parseBody(httpCode: Int, rawBody: String?): ApiError {
        if (!rawBody.isNullOrBlank()) {
            runCatching {
                val payload = json.decodeFromString<ApiErrorPayload>(rawBody)
                val fallbackMsg = payload.errors?.values?.firstOrNull()?.firstOrNull()
                    ?: defaultHttpMessage(httpCode)
                return ApiError(
                    httpCode = httpCode,
                    code = payload.code,
                    message = payload.message?.takeIf { it.isNotBlank() } ?: fallbackMsg,
                    fieldErrors = payload.errors.orEmpty(),
                    propertiList = payload.propertiList.orEmpty(),
                    requestId = payload.requestId,
                )
            }
        }
        return ApiError(
            httpCode = httpCode,
            message = defaultHttpMessage(httpCode),
        )
    }

    fun fromThrowable(t: Throwable): ApiError = when (t) {
        is HttpException -> {
            val body = runCatching { t.response()?.errorBody()?.string() }.getOrNull()
            parseBody(t.code(), body)
        }
        is SocketTimeoutException -> ApiError(
            message = "Koneksi ke server melebihi batas waktu. Pastikan server Laravel aktif.",
        )
        is ConnectException, is UnknownHostException -> ApiError(
            message = "Tidak dapat terhubung ke server API. Periksa koneksi, php artisan serve, atau adb reverse.",
        )
        is IOException -> ApiError(
            message = "Gangguan jaringan: ${t.localizedMessage ?: "periksa koneksi Anda."}",
        )
        else -> ApiError(
            message = t.localizedMessage ?: "Terjadi galat tak terduga.",
        )
    }

    private fun defaultHttpMessage(httpCode: Int): String = when (httpCode) {
        401 -> "Sesi login telah berakhir. Silakan masuk kembali."
        403 -> "Anda tidak memiliki izin untuk aksi ini."
        404 -> "Data tidak ditemukan pada properti aktif."
        422 -> "Periksa kembali isian formulir Anda."
        429 -> "Terlalu banyak percobaan. Tunggu sebentar lalu coba lagi."
        in 500..599 -> "Server mengalami gangguan ($httpCode). Coba lagi sebentar."
        else -> "Permintaan gagal (HTTP $httpCode)."
    }
}

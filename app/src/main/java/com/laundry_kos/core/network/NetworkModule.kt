package com.laundry_kos.core.network

import com.laundry_kos.BuildConfig
import com.laundry_kos.core.session.SessionStore
import com.laundry_kos.data.api.LaundryKosApi
import com.laundry_kos.data.dto.PropertiDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

sealed interface GlobalApiEvent {
    data class Unauthorized(val message: String) : GlobalApiEvent
    data class SessionRevoked(val code: String?, val message: String) : GlobalApiEvent
    data class PropertiRequired(val propertiList: List<PropertiDto>, val message: String) : GlobalApiEvent
}

class ApiEventBus {
    private val _events = MutableSharedFlow<GlobalApiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<GlobalApiEvent> = _events.asSharedFlow()

    fun emit(event: GlobalApiEvent) {
        _events.tryEmit(event)
    }
}

class AuthHeadersInterceptor(
    private val sessionStore: SessionStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("X-Request-Id", UUID.randomUUID().toString())

        val path = original.url.encodedPath
        val isLoginEndpoint = path.endsWith("/auth/login")

        if (!isLoginEndpoint) {
            sessionStore.currentToken?.let { token ->
                builder.header("Authorization", "Bearer $token")
            }
            sessionStore.currentPropertiId?.let { propertiId ->
                builder.header("X-Properti-Id", propertiId.toString())
            }
        }

        return chain.proceed(builder.build())
    }
}

object NetworkModule {
    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        isLenient = true
    }

    fun createApi(sessionStore: SessionStore): LaundryKosApi {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthHeadersInterceptor(sessionStore))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                redactHeader("Authorization")
            }
            clientBuilder.addInterceptor(logging)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return retrofit.create(LaundryKosApi::class.java)
    }
}

suspend inline fun <T> safeApiCall(
    eventBus: ApiEventBus? = null,
    crossinline block: suspend () -> T,
): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (t: Throwable) {
        val error = ApiErrorParser.fromThrowable(t)
        if (error.isAccountOrPropertyBlocked) {
            eventBus?.emit(GlobalApiEvent.SessionRevoked(error.code, error.message))
        } else if (error.isUnauthorized) {
            eventBus?.emit(GlobalApiEvent.Unauthorized(error.message))
        } else if (error.isPropertiBelumDipilih) {
            eventBus?.emit(GlobalApiEvent.PropertiRequired(error.propertiList, error.message))
        }
        ApiResult.Failure(error)
    }
}

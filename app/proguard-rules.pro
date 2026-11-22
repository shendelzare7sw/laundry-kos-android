# Laundry-Kos Production R8 / ProGuard Rules

# Pertahankan nama file & baris stacktrace crash untuk diagnosa produksi
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlinx Serialization: pertahankan serializer & companion untuk semua @Serializable DTO
-keep,includedescriptorclasses class com.laundry_kos.data.dto.**$$serializer { *; }
-keepclassmembers class com.laundry_kos.data.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.laundry_kos.data.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.laundry_kos.core.network.ApiErrorPayload** { *; }
-keep,includedescriptorclasses class com.laundry_kos.core.notifikasi.**$$serializer { *; }
-keepclassmembers class com.laundry_kos.core.notifikasi.** {
    *** Companion;
}
-keep class com.laundry_kos.core.notifikasi.ActivitySyncWorker { *; }
-keep class com.laundry_kos.core.notifikasi.LaundryFirebaseMessagingService { *; }

# Retrofit: pertahankan interface API dan signature suspend/generic
-keep,allowobfuscation,allowshrinking interface com.laundry_kos.data.api.LaundryKosApi { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp / Okio optional platform classes
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

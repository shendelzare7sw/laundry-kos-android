# Laundry-Kos Android

Aplikasi Native Android (**Kotlin + Jetpack Compose + Material 3**) untuk operasional **Laundry-Kos**, terintegrasi dengan backend REST API v1 Laravel.

## Stack Utama
- **UI:** Jetpack Compose (Material 3) + tema Marun (`#8B1A1A`), Emas (`#C8920E`), Latar Krem (`#F9F5F0`)
- **Networking:** Retrofit 2 + OkHttp + Kotlinx Serialization
- **Session Storage:** Jetpack DataStore Preferences (Sanctum Bearer Token & `X-Properti-Id`)
- **Target SDK:** `minSdk = 26`, `compileSdk = 35`, `targetSdk = 35`

## Menjalankan Lokal dengan HP Fisik (Tanpa AVD)

1. Jalankan backend Laravel di laptop:
   ```bash
   php artisan serve --port=8000
   ```
2. Hubungkan HP fisik via USB (USB Debugging aktif) lalu aktifkan port reverse:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
3. Default `BuildConfig.API_BASE_URL` pada build `debug` adalah `http://127.0.0.1:8000/api/v1/`.

### Mengganti Base URL (Opsional)
Tambahkan pada `local.properties` (tidak masuk Git):
```properties
# Contoh untuk Android Emulator:
API_BASE_URL_DEBUG=http://10.0.2.2:8000/api/v1/

# Contoh untuk HP fisik via Wi-Fi LAN:
# API_BASE_URL_DEBUG=http://192.168.1.10:8000/api/v1/

# URL Produksi untuk build release:
API_BASE_URL_RELEASE=https://domain-anda.com/api/v1/
```

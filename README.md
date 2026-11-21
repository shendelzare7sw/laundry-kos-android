# Laundry-Kos Android

Aplikasi Native Android (**Kotlin + Jetpack Compose + Material 3**) untuk operasional **Laundry-Kos**, terintegrasi penuh dengan 29 endpoint REST API v1 Laravel.

## Fitur Lengkap (Fase 0 – Fase 7)
- **Autentikasi Sanctum & Multi-Properti:** Login (`username` + `password` + `device_name`), simpan token & `X-Properti-Id` di Jetpack DataStore (dikecualikan dari cloud backup), pemilih/switcher properti untuk Owner/DevAdmin, serta switcher Server Dev pada build `debug`.
- **Beranda:** Ringkasan bulanan (`berat_centi`/`berat_label`, `Int` rupiah) & daftar peringatan kuota (`lewat` / `mendekati`) dengan tombol langsung WhatsApp.
- **Pencatatan Laundry:**
  - Filter periode (`bulan_ini`, `bulan_lalu`, `hari_ini`, `7_hari`, `semua`, `rentang`), filter jenis, pencarian, dan tombol **"Lihat lebih banyak"** (`?tampil=N`) tanpa pagination angka.
  - Form Catat & Edit dengan **1 combobox penghuni**, live preview `/laundry/info-kuota`, deteksi duplikat 1-tanggal-1-catatan, biaya otomatis/manual, multi-item terpisah dinamis, konfirmasi berat > 30 kg, dan idempotensi `ref` UUID.
  - Lengkapi Harga Otomatis, Tetapkan Pemilik (`tanpa_nama`), dan daftar Catatan Terhapus + Pulihkan.
- **Penghuni, Kontrak & Kuota:** Tab status (`Semua`, `Booking`, `Aktif`, `Keluar`), daftar penghuni baru (kamar kosong), detail penghuni, riwayat penempatan & kuota 6 bulan, edit data, ganti nomor WA, pindah kamar / batal pindah, keluar / batal keluar, dan penyesuaian kuota manual/reset.
- **Laporan, WA & Cetak PDF:** Format kartu **2 kolom**, jalur kamar (`307→310`), penanda merah K4, **edit inline tagihan lewat kuota**, tombol WA tagihan ringkas & Rekap WA via `Intent`, serta **Cetak / Simpan PDF A4** via `Android PrintManager`.
- **Pengaturan:** Aturan tarif & kuota berversi (`berlaku_mulai`), izin penjaga, template pesan WA (+ reset bawaan), ganti properti, dan logout.

## Pengujian di HP Fisik (Tanpa AVD)

1. Jalankan backend Laravel di laptop:
   ```bash
   php artisan serve --port=8000
   ```
2. Hubungkan HP fisik via USB (USB Debugging aktif) lalu aktifkan reverse port:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
3. Build & jalankan aplikasi dari Android Studio (atau `.\gradlew.bat installDebug`).
   - Default `BuildConfig.API_BASE_URL` build `debug` adalah `http://127.0.0.1:8000/api/v1/` (siap langsung untuk `adb reverse`).
   - Di layar Login build `debug`, tersedia tombol **Server Dev** untuk berganti cepat ke `10.0.2.2` (Emulator) atau IP LAN.

### Override Base URL via `local.properties` (Opsional)
```properties
API_BASE_URL_DEBUG=http://127.0.0.1:8000/api/v1/
API_BASE_URL_RELEASE=https://domain-anda.com/api/v1/
```

package com.laundry_kos.core.network

import com.laundry_kos.core.util.LaporanPdfPrinter
import com.laundry_kos.data.dto.AuthResponseDto
import com.laundry_kos.data.dto.LaporanResponse
import com.laundry_kos.data.dto.LaundryListResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiSerializationAndErrorTest {

    @Test
    fun parseError_handlesPropertiBelumDipilih422() {
        val body = """
            {
              "message": "Pilih properti aktif terlebih dahulu.",
              "code": "PROPERTI_BELUM_DIPILIH",
              "request_id": "req-123",
              "properti_list": [
                {"id": 1, "nama": "Kos Mawar", "status": "aktif"},
                {"id": 2, "nama": "Kos Melati", "status": "aktif"}
              ]
            }
        """.trimIndent()

        val err = ApiErrorParser.parseBody(422, body)
        assertTrue(err.isPropertiBelumDipilih)
        assertEquals(2, err.propertiList.size)
        assertEquals("Kos Mawar", err.propertiList.first().nama)
    }

    @Test
    fun parseError_handlesValidationErrors422() {
        val body = """
            {
              "message": "The given data was invalid.",
              "errors": {
                "tanggal": ["Budi sudah memiliki catatan laundry pada 21/11/2026."]
              }
            }
        """.trimIndent()

        val err = ApiErrorParser.parseBody(422, body)
        assertEquals("Budi sudah memiliki catatan laundry pada 21/11/2026.", err.firstFieldError("tanggal"))
        assertEquals("Budi sudah memiliki catatan laundry pada 21/11/2026.", err.summaryMessage())
    }

    @Test
    fun deserialize_authAndLaundryAndLaporanDtos() {
        val json = NetworkModule.json

        val authJson = """
            {
              "token": "1|secretToken",
              "token_type": "Bearer",
              "user": {"id": 5, "name": "Owner", "username": "owner", "role": "owner", "role_label": "Pemilik", "aktif": true},
              "properti_aktif": {"id": 1, "nama": "Kos Mawar", "status": "aktif"},
              "properti_list": [{"id": 1, "nama": "Kos Mawar", "status": "aktif"}],
              "permissions": {"catat_laundry": true, "sesuaikan_kuota": true}
            }
        """.trimIndent()
        val auth = json.decodeFromString<AuthResponseDto>(authJson)
        assertEquals("1|secretToken", auth.token)
        assertTrue(auth.permissions.catatLaundry)

        val laundryJson = """
            {
              "rentang": {"periode": "bulan_ini", "label": "November 2026"},
              "ringkasan": {"total_berat_centi": 2370, "total_berat_kg": "23,7", "total_biaya_utama": 192000, "total_bayar_terpisah": 25000, "jumlah_catatan": 10},
              "meta": {"ditampilkan": 20, "total": 108, "langkah": 20, "ada_lagi": true, "tampil_berikutnya": 40},
              "pratinjau_lengkapi_harga": {"jumlah": 2, "total": 32000},
              "jumlah_terhapus": 1,
              "data": [
                {
                  "id": 99,
                  "tanggal": "2026-11-21",
                  "jenis": "penghuni",
                  "penghuni_id": 10,
                  "penghuni_nama": "Budi",
                  "kamar_nomor": "307",
                  "berat_centi": 2370,
                  "berat_label": "23,7",
                  "biaya_utama": 24000,
                  "item_biaya": 15000,
                  "item": [{"id": 1, "nama": "Bedcover", "jumlah": 1, "biaya": 15000}],
                  "lewat_kuota": true,
                  "boleh_ubah": true,
                  "boleh_hapus": true
                }
              ]
            }
        """.trimIndent()
        val laundryRes = json.decodeFromString<LaundryListResponseDto>(laundryJson)
        assertTrue(laundryRes.meta.adaLagi)
        assertEquals(40, laundryRes.meta.tampilBerikutnya)
        assertEquals(2370, laundryRes.data.first().beratCenti)
        assertEquals("23,7", laundryRes.data.first().beratLabel)

        val laporanJson = """
            {
              "nama_properti": "Kos Mawar",
              "rentang": {"periode": "bulan_ini", "dari": "2026-11-01", "sampai": "2026-11-30", "label": "1 Nov 2026 - 30 Nov 2026", "bulan_acuan": "2026-11", "is_bulan_penuh": true},
              "dicetak_label": "21 Nov 2026",
              "kuota_gratis_centi": 2000,
              "kuota_gratis_label": "20,0",
              "ringkasan": {"total_kg_centi": 2370, "total_kg_label": "23,7", "jumlah_penghuni": 1, "total_biaya_utama": 192000, "total_tagihan_lewat": 30000, "total_bayar_terpisah": 15000},
              "kartu_penghuni": [
                {
                  "penghuni": {"id": 10, "nama": "Budi", "no_wa": "08123456789", "status": "aktif"},
                  "label_kamar": "307→310",
                  "jumlah_catatan": 3,
                  "total_kg_centi": 2370,
                  "total_kg_label": "23,7",
                  "biaya_utama": 192000,
                  "bayar_terpisah": 15000,
                  "jumlah_item_terpisah": 1,
                  "tagihan_lewat_kuota": 30000,
                  "kuota": {"penghuni_id": 10, "bulan": "2026-11", "terpakai_centi": 2370, "terpakai_kg": "23,7", "kuota_centi": 2000, "kuota_kg": "20", "status": "lewat", "tagihan_lewat_kuota": 30000},
                  "boleh_sesuaikan_kuota": true,
                  "baris": [
                    {"tipe": "utama", "laundry_id": 99, "tanggal": "2026-11-21", "tanggal_label": "21 Nov 2026", "berat_centi": 370, "berat_label": "3,7", "biaya": 30000, "merah": true}
                  ],
                  "wa_tagihan": {"pesan": "Tagihan Rp 45.000", "nomor": "628123456789", "url": "https://wa.me/628123456789?text=Tagihan", "total_tagihan": 45000}
                }
              ],
              "kartu_lainnya": [],
              "rekap_wa": {"pesan": "Rekap Kos Mawar", "url": "https://wa.me/?text=Rekap", "terlalu_panjang": false, "jumlah_karakter": 15}
            }
        """.trimIndent()
        val laporan = json.decodeFromString<LaporanResponse>(laporanJson)
        assertEquals("307→310", laporan.kartuPenghuni.first().labelKamar)
        val html = LaporanPdfPrinter.buildPrintHtml(laporan)
        assertTrue(html.contains("TOTAL LAUNDRY"))
        assertTrue(html.contains("JUMLAH PENGHUNI"))
        assertTrue(html.contains("MENDEKATI / LEWAT"))
        assertTrue(html.contains("307→310"))
        assertTrue(html.contains("entry-row merah"))
        assertTrue(html.contains("Subtotal semua"))
        assertTrue(html.contains("Rp30.000"))
    }
}

package com.laundry_kos.data.api

import com.laundry_kos.data.dto.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LaundryKosApi {

    // --- Auth & Properti ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(): MessageResponseDto

    @GET("me")
    suspend fun me(): MeResponseDto

    @GET("properti")
    suspend fun properti(): PropertiListResponseDto

    @GET("kamar")
    suspend fun kamar(@Query("kosong") kosong: Int? = null): KamarListResponseDto

    // --- Beranda ---
    @GET("beranda")
    suspend fun beranda(@Query("bulan") bulan: String? = null): BerandaResponseDto

    // --- Pengaturan ---
    @GET("pengaturan")
    suspend fun pengaturan(@Query("tanggal") tanggal: String? = null): PengaturanResponseDto

    @PUT("pengaturan/aturan")
    suspend fun simpanAturan(@Body request: SimpanAturanRequestDto): MessageResponseDto

    @PUT("pengaturan/izin-penjaga")
    suspend fun simpanIzinPenjaga(@Body request: SimpanIzinPenjagaRequestDto): MessageResponseDto

    @PUT("pengaturan/template-wa")
    suspend fun simpanTemplateWa(@Body request: SimpanTemplateWaRequestDto): MessageResponseDto

    @POST("pengaturan/template-wa/reset")
    suspend fun resetTemplateWa(@Body request: ResetTemplateWaRequestDto): MessageResponseDto

    // --- Penghuni, Kontrak & Kuota ---
    @GET("penghuni")
    suspend fun penghuniList(
        @Query("tab") tab: String = "semua",
        @Query("cari") cari: String? = null,
        @Query("bulan") bulan: String? = null,
    ): PenghuniListResponseDto

    @POST("penghuni")
    suspend fun tambahPenghuni(@Body request: MasukPenghuniRequestDto): PenghuniMutasiResponseDto

    @GET("penghuni/{id}")
    suspend fun detailPenghuni(
        @Path("id") id: Int,
        @Query("bulan") bulan: String? = null,
    ): PenghuniDetailResponseDto

    @PUT("penghuni/{id}")
    suspend fun ubahPenghuni(
        @Path("id") id: Int,
        @Body request: UbahPenghuniRequestDto,
    ): PenghuniMutasiResponseDto

    @POST("penghuni/{id}/pindah-kamar")
    suspend fun pindahKamar(
        @Path("id") id: Int,
        @Body request: PindahKamarRequestDto,
    ): PenempatanMutasiResponseDto

    @POST("penghuni/{id}/batalkan-pindah")
    suspend fun batalkanPindah(@Path("id") id: Int): PenempatanMutasiResponseDto

    @POST("penghuni/{id}/keluar")
    suspend fun keluarPenghuni(
        @Path("id") id: Int,
        @Body request: KeluarPenghuniRequestDto,
    ): PenghuniMutasiResponseDto

    @POST("penghuni/{id}/batalkan-keluar")
    suspend fun batalkanKeluar(@Path("id") id: Int): PenghuniMutasiResponseDto

    @POST("penghuni/{id}/ganti-nomor")
    suspend fun gantiNomor(
        @Path("id") id: Int,
        @Body request: GantiNomorRequestDto,
    ): PenghuniMutasiResponseDto

    @POST("penghuni/{id}/penyesuaian-kuota")
    suspend fun simpanPenyesuaianKuota(
        @Path("id") id: Int,
        @Body request: PenyesuaianKuotaRequestDto,
    ): PenyesuaianKuotaResponseDto

    // --- Pencatatan Laundry ---
    @GET("laundry")
    suspend fun laundryList(
        @Query("periode") periode: String = "bulan_ini",
        @Query("dari") dari: String? = null,
        @Query("sampai") sampai: String? = null,
        @Query("cari") cari: String? = null,
        @Query("jenis") jenis: String? = null,
        @Query("tampil") tampil: Int = 20,
    ): LaundryListResponseDto

    @GET("laundry/info-kuota")
    suspend fun infoKuota(
        @Query("tanggal") tanggal: String,
        @Query("penghuni_id") penghuniId: Int? = null,
        @Query("berat_kg") beratKg: String? = null,
        @Query("kecuali_id") kecualiId: Int? = null,
    ): InfoKuotaResponseDto

    @POST("laundry")
    suspend fun tambahLaundry(@Body request: SimpanLaundryRequestDto): LaundryMutasiResponseDto

    @GET("laundry/{id}")
    suspend fun detailLaundry(@Path("id") id: Int): LaundryDetailResponseDto

    @PUT("laundry/{id}")
    suspend fun ubahLaundry(
        @Path("id") id: Int,
        @Body request: SimpanLaundryRequestDto,
    ): LaundryMutasiResponseDto

    @DELETE("laundry/{id}")
    suspend fun hapusLaundry(@Path("id") id: Int): HapusLaundryResponseDto

    @GET("laundry/terhapus")
    suspend fun laundryTerhapus(@Query("tampil") tampil: Int = 15): TerhapusListResponseDto

    @POST("laundry/{id}/pulihkan")
    suspend fun pulihkanLaundry(@Path("id") id: Int): LaundryMutasiResponseDto

    @POST("laundry/{id}/tetapkan-pemilik")
    suspend fun tetapkanPemilik(
        @Path("id") id: Int,
        @Body request: TetapkanPemilikRequestDto,
    ): LaundryMutasiResponseDto

    @POST("laundry/lengkapi-harga")
    suspend fun lengkapiHarga(): LengkapiHargaResponseDto

    // --- Laporan & WhatsApp ---
    @GET("laporan")
    suspend fun laporan(
        @Query("periode") periode: String = "bulan_ini",
        @Query("dari") dari: String? = null,
        @Query("sampai") sampai: String? = null,
        @Query("penghuni_id") penghuniId: Int? = null,
        @Query("rinci") rinci: Int = 0,
    ): LaporanResponse

    @GET("wa/tagihan/{penghuni}")
    suspend fun waTagihan(
        @Path("penghuni") penghuniId: Int,
        @Query("bulan") bulan: String? = null,
    ): WaMessageDto

    @GET("wa/peringatan/{penghuni}")
    suspend fun waPeringatan(
        @Path("penghuni") penghuniId: Int,
        @Query("bulan") bulan: String? = null,
    ): WaMessageDto
}

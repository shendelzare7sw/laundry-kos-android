package com.laundry_kos.core.notifikasi

import android.content.Context
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.data.dto.BerandaResponseDto
import com.laundry_kos.data.dto.LaundryDto
import com.laundry_kos.data.dto.PenghuniDto
import com.laundry_kos.data.repository.LaundryKosRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class ActivitySyncOutcome(
    val newEvents: List<StoredNotification>,
    val beranda: BerandaResponseDto? = null,
)

class ActivitySyncEngine(
    private val context: Context,
    private val repository: LaundryKosRepository,
    private val notificationStore: NotificationStore,
) {
    /**
     * Membandingkan data terbaru dari server dengan snapshot lokal terakhir untuk mendeteksi
     * perubahan antar role (input/ubah/hapus laundry, perubahan penghuni, dan peringatan kuota)
     * lalu menampilkan notifikasi ke panel sistem Android.
     */
    suspend fun syncActiveProperty(
        propertiId: Int,
        currentUserId: Int?,
        emitSystemNotifications: Boolean = true,
    ): ActivitySyncOutcome = withContext(Dispatchers.IO) {
        val storeState = notificationStore.current()
        val prev = storeState.snapshots[propertiId] ?: PropertyActivitySnapshot()

        val (laundryRes, penghuniRes, berandaRes) = coroutineScope {
            val laundryDeferred = async { repository.fetchLaundryList(periode = "7_hari", tampil = 40) }
            val penghuniDeferred = async { repository.fetchPenghuniList(tab = "semua") }
            val berandaDeferred = async { repository.fetchBeranda(null) }
            Triple(laundryDeferred.await(), penghuniDeferred.await(), berandaDeferred.await())
        }

        val berandaData = (berandaRes as? ApiResult.Success)?.data
        if (laundryRes !is ApiResult.Success && penghuniRes !is ApiResult.Success && berandaRes !is ApiResult.Success) {
            return@withContext ActivitySyncOutcome(emptyList(), null)
        }

        val nowMs = System.currentTimeMillis()
        val newEvents = mutableListOf<StoredNotification>()

        // 1. Diff Catatan Laundry (7 hari terakhir)
        val nextLaundryMap: Map<Int, LaundrySnapshotItem> = if (laundryRes is ApiResult.Success) {
            val currentList = laundryRes.data.data
            val currentMap = currentList.associate { item ->
                item.id to LaundrySnapshotItem(
                    label = labelForLaundry(item),
                    tanggal = item.tanggal,
                    fingerprint = fingerprintForLaundry(item),
                )
            }

            if (prev.initialized) {
                val cutoffDate = LocalDate.now(DateUtil.ZONE_JAKARTA).minusDays(6).toString()

                // Input baru & perubahan
                currentList.forEach { item ->
                    val oldSnap = prev.laundry[item.id]
                    val newSnap = currentMap[item.id] ?: return@forEach
                    val byOtherUser = currentUserId == null || item.dicatatOleh == null || item.dicatatOleh != currentUserId

                    if (oldSnap == null && byOtherUser) {
                        val pencatat = item.dicatatOlehNama?.takeIf { it.isNotBlank() } ?: "Staf"
                        val detail = buildLaundrySummary(item)
                        newEvents.add(
                            StoredNotification(
                                id = "act_laundry_add_${propertiId}_${item.id}_$nowMs",
                                propertiId = propertiId,
                                type = "ACTIVITY",
                                title = "Laundry baru dicatat ($pencatat)",
                                subtitle = "${newSnap.label} · $detail",
                                timestampMs = nowMs,
                                penghuniId = item.penghuniId,
                                laundryId = item.id,
                            ),
                        )
                    } else if (oldSnap != null && oldSnap.fingerprint != newSnap.fingerprint) {
                        val detail = buildLaundrySummary(item)
                        newEvents.add(
                            StoredNotification(
                                id = "act_laundry_upd_${propertiId}_${item.id}_$nowMs",
                                propertiId = propertiId,
                                type = "ACTIVITY",
                                title = "Catatan laundry diperbarui",
                                subtitle = "${newSnap.label} · $detail",
                                timestampMs = nowMs,
                                penghuniId = item.penghuniId,
                                laundryId = item.id,
                            ),
                        )
                    }
                }

                // Penghapusan catatan dalam rentang 7 hari terakhir
                prev.laundry.forEach { (id, oldSnap) ->
                    if (id !in currentMap && oldSnap.tanggal >= cutoffDate) {
                        newEvents.add(
                            StoredNotification(
                                id = "act_laundry_del_${propertiId}_${id}_$nowMs",
                                propertiId = propertiId,
                                type = "WARN",
                                title = "Catatan laundry dihapus",
                                subtitle = "${oldSnap.label} (${DateUtil.formatIsoDateLabel(oldSnap.tanggal)}) telah dihapus.",
                                timestampMs = nowMs,
                            ),
                        )
                    }
                }
            }
            currentMap
        } else {
            prev.laundry
        }

        // 2. Diff Data Penghuni (masuk, pindah kamar, keluar, ubah profil)
        val nextPenghuniMap: Map<Int, PenghuniSnapshotItem> = if (penghuniRes is ApiResult.Success) {
            val currentList = penghuniRes.data.data
            val currentMap = currentList.associate { p ->
                p.id to PenghuniSnapshotItem(
                    nama = p.nama,
                    status = p.status,
                    kamarNomor = p.kamarNomor,
                    fingerprint = fingerprintForPenghuni(p),
                )
            }

            if (prev.initialized) {
                currentList.forEach { p ->
                    val oldSnap = prev.penghuni[p.id]
                    val newSnap = currentMap[p.id] ?: return@forEach
                    if (oldSnap == null) {
                        val kamarTxt = p.kamarNomor?.let { "Kamar $it" } ?: "tanpa kamar"
                        newEvents.add(
                            StoredNotification(
                                id = "act_penghuni_add_${propertiId}_${p.id}_$nowMs",
                                propertiId = propertiId,
                                type = "INFO",
                                title = "Penghuni baru didaftarkan",
                                subtitle = "${p.nama} ($kamarTxt · ${p.statusLabel.ifBlank { p.status }})",
                                timestampMs = nowMs,
                                penghuniId = p.id,
                            ),
                        )
                    } else if (oldSnap.fingerprint != newSnap.fingerprint) {
                        val titleAndSub = when {
                            oldSnap.status != newSnap.status && newSnap.status == "keluar" ->
                                "Penghuni dicatat keluar" to "${p.nama} (eks Kamar ${oldSnap.kamarNomor ?: "-"}) berstatus Keluar."
                            oldSnap.kamarNomor != newSnap.kamarNomor ->
                                "Penghuni pindah kamar" to "${p.nama}: Kamar ${oldSnap.kamarNomor ?: "-"} → ${newSnap.kamarNomor ?: "-"}."
                            else ->
                                "Data penghuni diperbarui" to "${p.nama} (Kamar ${newSnap.kamarNomor ?: "-"}) telah diperbarui."
                        }
                        newEvents.add(
                            StoredNotification(
                                id = "act_penghuni_upd_${propertiId}_${p.id}_$nowMs",
                                propertiId = propertiId,
                                type = "INFO",
                                title = titleAndSub.first,
                                subtitle = titleAndSub.second,
                                timestampMs = nowMs,
                                penghuniId = p.id,
                            ),
                        )
                    }
                }
            }
            currentMap
        } else {
            prev.penghuni
        }

        // 3. Diff Peringatan Kuota baru (mendekati / melewati kuota)
        val nextQuotaMap: Map<Int, String> = if (berandaRes is ApiResult.Success) {
            val alerts = berandaRes.data.daftarPeringatan
            val currentMap = alerts.associate { a ->
                a.penghuniId to "${a.kuota.status}:${a.kuota.terpakaiCenti}"
            }

            if (prev.initialized) {
                alerts.forEach { a ->
                    val oldVal = prev.quotaStatus[a.penghuniId]
                    val newVal = currentMap[a.penghuniId]
                    if (oldVal == null || (oldVal.startsWith("mendekati:") && a.kuota.status == "lewat")) {
                        val isLewat = a.kuota.status == "lewat"
                        newEvents.add(
                            StoredNotification(
                                id = "act_kuota_${propertiId}_${a.penghuniId}_${a.kuota.status}_$nowMs",
                                propertiId = propertiId,
                                type = if (isLewat) "DANGER" else "WARN",
                                title = if (isLewat) {
                                    "${a.nama} (Kamar ${a.kamarNomor}) melewati kuota"
                                } else {
                                    "${a.nama} (Kamar ${a.kamarNomor}) mendekati kuota"
                                },
                                subtitle = if (isLewat) {
                                    "Pemakaian ${a.kuota.terpakaiKg}/${a.kuota.kuotaKg} kg · Tagihan ${Formatters.formatRupiah(a.kuota.tagihanLewatKuota)}"
                                } else {
                                    "Pemakaian ${a.kuota.terpakaiKg}/${a.kuota.kuotaKg} kg · Sisa ${a.kuota.sisaKg} kg"
                                },
                                timestampMs = nowMs,
                                penghuniId = a.penghuniId,
                            ),
                        )
                    }
                }
            }
            currentMap
        } else {
            prev.quotaStatus
        }

        val updatedSnapshot = PropertyActivitySnapshot(
            initialized = true,
            laundry = nextLaundryMap,
            penghuni = nextPenghuniMap,
            quotaStatus = nextQuotaMap,
        )

        notificationStore.recordSyncResult(
            propertiId = propertiId,
            newSnapshot = updatedSnapshot,
            newEvents = newEvents,
        )

        if (emitSystemNotifications && newEvents.isNotEmpty()) {
            SystemNotificationHelper.showBatch(context, newEvents)
        }

        ActivitySyncOutcome(
            newEvents = newEvents,
            beranda = berandaData,
        )
    }

    private fun labelForLaundry(item: LaundryDto): String = when (item.jenis) {
        "penghuni" -> {
            val kamar = item.kamarNomor?.let { "Kamar $it · " } ?: ""
            "$kamar${item.penghuniNama ?: "Penghuni"}"
        }
        "gedung" -> "Laundry Gedung"
        else -> item.namaTanpaNama?.takeIf { it.isNotBlank() }?.let { "Tanpa Nama · $it" } ?: "Tanpa Nama"
    }

    private fun buildLaundrySummary(item: LaundryDto): String {
        val parts = mutableListOf<String>()
        val berat = Formatters.formatBeratKg(item.beratCenti, item.beratLabel)
        if (berat != "-") parts.add(berat)
        if (item.biayaUtama != null && item.biayaUtama > 0) {
            parts.add(Formatters.formatRupiah(item.biayaUtama))
        }
        if (item.itemBiaya > 0) {
            parts.add("+Terpisah ${Formatters.formatRupiah(item.itemBiaya)}")
        }
        return parts.joinToString(" · ").ifBlank { DateUtil.formatIsoDateLabel(item.tanggal) }
    }

    private fun fingerprintForLaundry(item: LaundryDto): String =
        listOf(
            item.tanggal,
            item.jenis,
            item.penghuniId?.toString().orEmpty(),
            item.kamarNomor.orEmpty(),
            item.namaTanpaNama.orEmpty(),
            item.beratCenti?.toString().orEmpty(),
            item.biayaUtama?.toString().orEmpty(),
            item.itemBiaya.toString(),
            item.item.size.toString(),
            item.catatan.orEmpty(),
        ).joinToString("|")

    private fun fingerprintForPenghuni(p: PenghuniDto): String =
        listOf(
            p.nama,
            p.alias.orEmpty(),
            p.status,
            p.kamarNomor.orEmpty(),
            p.noWa.orEmpty(),
        ).joinToString("|")
}

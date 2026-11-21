package com.laundry_kos.feature.beranda

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.KuotaProgressBar
import com.laundry_kos.core.designsystem.KuotaVisualStatus
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.MonthSelectorBar
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.WhatsAppIntent
import com.laundry_kos.data.dto.PeringatanKuotaItemDto
import com.laundry_kos.data.dto.PermissionsDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    viewModel: BerandaViewModel,
    permissions: PermissionsDto,
    onCatatLaundry: () -> Unit,
    onOpenLaundryList: () -> Unit,
    onOpenPenghuniList: () -> Unit,
    onOpenPenghuniDetail: (Int) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = uiState.refreshing,
        onRefresh = { viewModel.load(isRefresh = true) },
        modifier = Modifier.fillMaxSize(),
    ) {
        if (uiState.loading && uiState.data == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@PullToRefreshBox
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 3 Tombol Aksi Cepat persis beranda/index.blade.php (+ Catat, Laundry, Penghuni)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (permissions.catatLaundry) {
                        Button(
                            onClick = onCatatLaundry,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Catat", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    OutlinedButton(
                        onClick = onOpenLaundryList,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        border = BorderStroke(1.dp, ext.lineStrong),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.Default.LocalLaundryService, contentDescription = null, modifier = Modifier.size(15.dp), tint = ext.muted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Laundry", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    OutlinedButton(
                        onClick = onOpenPenghuniList,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        border = BorderStroke(1.dp, ext.lineStrong),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(15.dp), tint = ext.muted)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Penghuni", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            item {
                MonthSelectorBar(
                    bulanYm = uiState.bulan,
                    onChangeBulan = viewModel::changeBulan,
                )
            }

            if (uiState.error != null) {
                item {
                    ErrorBannerCard(
                        message = uiState.error!!,
                        onRetry = { viewModel.load(isRefresh = false) },
                    )
                }
            }

            val data = uiState.data
            if (data != null) {
                val r = data.ringkasan
                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            KpiMetricCard(
                                label = "Total Berat",
                                value = Formatters.formatBeratKg(r.totalBeratCenti, r.totalBeratLabel),
                                modifier = Modifier.weight(1f),
                            )
                            KpiMetricCard(
                                label = "Penghuni Mencatat",
                                value = "${r.jumlahPenghuniMencatat} orang",
                                modifier = Modifier.weight(1f),
                            )
                            KpiMetricCard(
                                label = "Biaya Utama",
                                value = Formatters.formatRupiah(r.totalBiayaUtama),
                                valueColor = ext.ok,
                                modifier = Modifier.weight(1f),
                            )
                            KpiMetricCard(
                                label = "Bayar Terpisah",
                                value = Formatters.formatRupiah(r.totalBayarTerpisah),
                                valueColor = ext.onGold,
                                containerColor = ext.goldContainer,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                KpiMetricCard(
                                    label = "Total Berat",
                                    value = Formatters.formatBeratKg(r.totalBeratCenti, r.totalBeratLabel),
                                    modifier = Modifier.weight(1f),
                                )
                                KpiMetricCard(
                                    label = "Penghuni Mencatat",
                                    value = "${r.jumlahPenghuniMencatat} orang",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                KpiMetricCard(
                                    label = "Biaya Utama",
                                    value = Formatters.formatRupiah(r.totalBiayaUtama),
                                    valueColor = ext.ok,
                                    modifier = Modifier.weight(1f),
                                )
                                KpiMetricCard(
                                    label = "Bayar Terpisah",
                                    value = Formatters.formatRupiah(r.totalBayarTerpisah),
                                    valueColor = ext.onGold,
                                    containerColor = ext.goldContainer,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = ext.warn,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Peringatan Kuota (${data.daftarPeringatan.size})",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Text(
                            text = data.rentangBulan.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.muted,
                        )
                    }
                }

                if (data.daftarPeringatan.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Semua kuota masih aman",
                            subtitle = "Belum ada penghuni yang mendekati atau melewati kuota pada ${data.rentangBulan.label}.",
                        )
                    }
                } else {
                    if (isWide) {
                        items(data.daftarPeringatan.chunked(2)) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                pair.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        PeringatanKuotaCard(
                                            item = item,
                                            onClick = { onOpenPenghuniDetail(item.penghuniId) },
                                            onSendWa = {
                                                WhatsAppIntent.openUrlOrCopy(
                                                    context = context,
                                                    url = item.waUrl,
                                                    label = "Peringatan Kuota ${item.nama}",
                                                )
                                            },
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        items(data.daftarPeringatan, key = { it.penghuniId }) { item ->
                            PeringatanKuotaCard(
                                item = item,
                                onClick = { onOpenPenghuniDetail(item.penghuniId) },
                                onSendWa = {
                                    WhatsAppIntent.openUrlOrCopy(
                                        context = context,
                                        url = item.waUrl,
                                        label = "Peringatan Kuota ${item.nama}",
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
) {
    LaundryCard(
        modifier = modifier,
        containerColor = containerColor,
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = LaundryKosTheme.extendedColors.muted,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
            )
        }
    }
}

@Composable
private fun PeringatanKuotaCard(
    item: PeringatanKuotaItemDto,
    onClick: () -> Unit,
    onSendWa: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val visualStatus = KuotaVisualStatus.fromApi(item.kuota.status)
    val isLewat = visualStatus == KuotaVisualStatus.LEWAT

    LaundryCard(
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kamar ${item.kamarNomor} · ${item.nama}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = "${item.kuota.terpakaiKg} / ${item.kuota.kuotaKg} kg (${item.kuota.persen}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                }
                StatusBadge(
                    text = if (isLewat) "Lewat ${item.kuota.kelebihanKg} kg" else "Mendekati",
                    containerColor = if (isLewat) ext.dangerContainer else ext.warnContainer,
                    contentColor = if (isLewat) ext.danger else ext.onGold,
                )
            }

            KuotaProgressBar(persen = item.kuota.persenBar, status = visualStatus)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val infoTagihan = if (item.kuota.tagihanLewatKuota > 0) {
                    "Tagihan lewat: ${Formatters.formatRupiah(item.kuota.tagihanLewatKuota)}"
                } else if (item.kuota.diBawahToleransi) {
                    "Masih di bawah toleransi (${item.kuota.toleransiKg} kg)"
                } else {
                    "Sisa kuota: ${item.kuota.sisaKg} kg"
                }
                Text(
                    text = infoTagihan,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.kuota.tagihanLewatKuota > 0) ext.danger else ext.muted,
                    modifier = Modifier.weight(1f),
                )

                if (!item.waUrl.isNullOrBlank()) {
                    Button(
                        onClick = onSendWa,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ext.whatsapp,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_whatsapp),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WA", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

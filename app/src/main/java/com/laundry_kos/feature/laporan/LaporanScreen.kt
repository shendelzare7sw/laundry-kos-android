package com.laundry_kos.feature.laporan

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.LaporanPdfPrinter
import com.laundry_kos.core.util.WhatsAppIntent
import com.laundry_kos.data.dto.LaporanKartuLainnyaDto
import com.laundry_kos.data.dto.LaporanKartuPenghuniDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    viewModel: LaporanViewModel,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.refreshing,
            onRefresh = { viewModel.load(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            val laporan = uiState.data
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Filter Periode & Mode Rinci
                item {
                    LaundryCard(contentPadding = PaddingValues(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(
                                    "bulan_ini" to "Bulan Ini",
                                    "bulan_lalu" to "Bulan Lalu",
                                    "rentang" to "Rentang",
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = uiState.periode == key,
                                        onClick = { viewModel.setPeriode(key) },
                                        label = { Text(label) },
                                    )
                                }
                            }

                            if (uiState.periode == "rentang") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    DatePickerField(
                                        label = "Dari",
                                        valueIso = uiState.dari,
                                        onValueSelected = { viewModel.setRentang(it, uiState.sampai) },
                                        modifier = Modifier.weight(1f),
                                    )
                                    DatePickerField(
                                        label = "Sampai",
                                        valueIso = uiState.sampai,
                                        onValueSelected = { viewModel.setRentang(uiState.dari, it) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Tampilkan Rincian Tanggal", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = uiState.rinci,
                                    onCheckedChange = viewModel::toggleRinci,
                                )
                            }

                            if (laporan != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Button(
                                        onClick = { LaporanPdfPrinter.printLaporan(context, laporan) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                        ),
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cetak / PDF", style = MaterialTheme.typography.labelMedium)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val rekap = laporan.rekapWa
                                            if (!rekap.url.isNullOrBlank()) {
                                                WhatsAppIntent.openUrlOrCopy(context, rekap.url, rekap.pesan, "Rekap WA")
                                            } else {
                                                WhatsAppIntent.sharePlainText(context, rekap.pesan, "Rekap Laundry ${laporan.rentang.label}")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (laporan.rekapWa.terlaluPanjang) Icons.Default.Share else Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rekap WA", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        ErrorBannerCard(message = uiState.error!!, onRetry = { viewModel.load() })
                    }
                }

                if (uiState.loading && laporan == null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (laporan != null) {
                    // Ringkasan Laporan
                    item {
                        val r = laporan.ringkasan
                        LaundryCard(containerColor = ext.surfaceAlt, contentPadding = PaddingValues(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(laporan.namaProperti, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(laporan.rentang.label, style = MaterialTheme.typography.labelSmall, color = ext.muted)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Total: ${r.totalKgLabel} kg (${r.jumlahPenghuni} penghuni)", style = MaterialTheme.typography.bodySmall)
                                    Text("Kuota gratis: ${laporan.kuotaGratisLabel} kg", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Utama: ${Formatters.formatRupiah(r.totalBiayaUtama)}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Lewat: ${Formatters.formatRupiah(r.totalTagihanLewat)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (r.totalTagihanLewat > 0) ext.danger else ext.muted,
                                    )
                                }
                                Text(
                                    text = "Bayar terpisah: ${Formatters.formatRupiah(r.totalBayarTerpisah)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.onGold,
                                )
                            }
                        }
                    }

                    if (laporan.kartuPenghuni.isEmpty() && laporan.kartuLainnya.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "Belum ada data laporan",
                                subtitle = "Tidak ada catatan laundry pada periode ${laporan.rentang.label}.",
                            )
                        }
                    } else {
                        // Format Kartu 2 Kolom untuk Penghuni
                        val pairs = laporan.kartuPenghuni.chunked(2)
                        items(pairs) { rowCards ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                rowCards.forEach { kartu ->
                                    LaporanPenghuniCard(
                                        kartu = kartu,
                                        bulanAcuan = laporan.rentang.bulanAcuan.ifBlank { kartu.kuota.bulan },
                                        rinci = uiState.rinci,
                                        isEditing = uiState.editingPenghuniId == kartu.penghuni.id,
                                        inlineNominal = uiState.inlineNominal,
                                        inlineSaving = uiState.inlineSaving,
                                        onStartInlineEdit = {
                                            viewModel.startInlineEdit(kartu.penghuni.id, kartu.tagihanLewatKuota)
                                        },
                                        onInlineNominalChange = viewModel::updateInlineNominal,
                                        onSaveInline = { reset ->
                                            viewModel.saveInlinePenyesuaian(
                                                penghuniId = kartu.penghuni.id,
                                                bulan = laporan.rentang.bulanAcuan.ifBlank { kartu.kuota.bulan },
                                                reset = reset,
                                            )
                                        },
                                        onCancelInline = viewModel::cancelInlineEdit,
                                        onSendWa = {
                                            kartu.waTagihan?.let { wa ->
                                                WhatsAppIntent.openUrlOrCopy(context, wa.url, wa.pesan, "Tagihan ${kartu.penghuni.nama}")
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (rowCards.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        // Kartu Lainnya (GDG / Tanpa Nama)
                        if (laporan.kartuLainnya.isNotEmpty()) {
                            items(laporan.kartuLainnya, key = { it.jenis }) { lain ->
                                LaporanLainnyaCard(kartu = lain, rinci = uiState.rinci)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaporanPenghuniCard(
    kartu: LaporanKartuPenghuniDto,
    bulanAcuan: String,
    rinci: Boolean,
    isEditing: Boolean,
    inlineNominal: String,
    inlineSaving: Boolean,
    onStartInlineEdit: () -> Unit,
    onInlineNominalChange: (String) -> Unit,
    onSaveInline: (reset: Boolean) -> Unit,
    onCancelInline: () -> Unit,
    onSendWa: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    val isLewat = kartu.kuota.status == "lewat"

    LaundryCard(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header nama + jalur kamar (mis. 307→310)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = kartu.penghuni.nama,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(4.dp))
                StatusBadge(
                    text = kartu.labelKamar,
                    containerColor = ext.goldContainer,
                    contentColor = ext.onGold,
                )
            }

            Text(
                text = "${kartu.totalKgLabel} kg · ${kartu.jumlahCatatan}x",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLewat) ext.danger else ext.muted,
            )

            // Rincian baris (tampil ringkas atau penuh saat rinci=true, penanda merah K4)
            if (kartu.baris.isNotEmpty() && rinci) {
                HorizontalDivider(color = ext.line)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    kartu.baris.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            val left = if (row.tipe == "item") {
                                "${row.tanggalLabel.take(6)} ${row.namaItem ?: "Item"}"
                            } else {
                                "${row.tanggalLabel.take(6)} · ${row.beratLabel ?: "-"}kg"
                            }
                            Text(
                                text = left,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (row.merah) ext.danger else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (row.merah) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = Formatters.formatRupiah(row.biaya, "-"),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = if (row.merah) ext.danger else ext.muted,
                                fontWeight = if (row.merah) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ext.line)

            // Ringkasan Biaya & Edit Inline Tagihan Lewat Kuota
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ext.surfaceAlt,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Utama", style = MaterialTheme.typography.labelSmall, color = ext.muted)
                        Text(Formatters.formatRupiah(kartu.biayaUtama), style = MaterialTheme.typography.labelSmall)
                    }

                    if (kartu.bayarTerpisah > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Terpisah", style = MaterialTheme.typography.labelSmall, color = ext.onGold)
                            Text(
                                text = Formatters.formatRupiah(kartu.bayarTerpisah),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ext.onGold,
                            )
                        }
                    }

                    // Tagihan lewat kuota + tombol edit inline
                    if (isEditing) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = inlineNominal,
                                onValueChange = onInlineNominalChange,
                                label = { Text("Lewat Kuota ($bulanAcuan)", fontSize = 10.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                if (kartu.kuota.nominalPenyesuaian != null) {
                                    IconButton(
                                        onClick = { onSaveInline(true) },
                                        enabled = !inlineSaving,
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset otomatis", modifier = Modifier.size(16.dp), tint = ext.warn)
                                    }
                                }
                                IconButton(
                                    onClick = onCancelInline,
                                    enabled = !inlineSaving,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Batal", modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onSaveInline(false) },
                                    enabled = !inlineSaving && inlineNominal.toIntOrNull() != null,
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Simpan", modifier = Modifier.size(16.dp), tint = ext.ok)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (kartu.kuota.nominalPenyesuaian != null) "Lewat*" else "Lewat",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (kartu.tagihanLewatKuota > 0) ext.danger else ext.muted,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Formatters.formatRupiah(kartu.tagihanLewatKuota),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (kartu.tagihanLewatKuota > 0) ext.danger else ext.muted,
                                )
                                if (kartu.bolehSesuaikanKuota && (isLewat || kartu.kuota.nominalPenyesuaian != null)) {
                                    IconButton(
                                        onClick = onStartInlineEdit,
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit tagihan lewat kuota",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tombol WA Tagihan Ringkas (hanya bila total tagihan > 0)
            val totalTagihanWa = kartu.waTagihan?.totalTagihan ?: 0
            if (totalTagihanWa > 0) {
                Button(
                    onClick = onSendWa,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ext.whatsapp,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_whatsapp),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WA ${Formatters.formatRupiah(totalTagihanWa)}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LaporanLainnyaCard(
    kartu: LaporanKartuLainnyaDto,
    rinci: Boolean,
) {
    val ext = LaundryKosTheme.extendedColors
    LaundryCard(containerColor = ext.surfaceAlt, contentPadding = PaddingValues(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(kartu.judul, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                StatusBadge(
                    text = "${kartu.totalKgLabel} kg · ${kartu.jumlahCatatan}x",
                    containerColor = ext.goldContainer,
                    contentColor = ext.onGold,
                )
            }
            if (rinci && kartu.baris.isNotEmpty()) {
                HorizontalDivider(color = ext.line)
                kartu.baris.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val label = if (row.tipe == "item") {
                            "${row.tanggalLabel} · Item: ${row.namaItem ?: "-"}"
                        } else {
                            "${row.tanggalLabel} · ${row.beratLabel ?: "-"} kg"
                        }
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Text(Formatters.formatRupiah(row.biaya), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Biaya utama: ${Formatters.formatRupiah(kartu.biayaUtama)}", style = MaterialTheme.typography.labelMedium)
                if (kartu.bayarTerpisah > 0) {
                    Text("Terpisah: ${Formatters.formatRupiah(kartu.bayarTerpisah)}", style = MaterialTheme.typography.labelMedium, color = ext.onGold)
                }
            }
        }
    }
}

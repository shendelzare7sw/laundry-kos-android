package com.laundry_kos.feature.laporan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.DottedLeaderRow
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.LaporanPdfPrinter
import com.laundry_kos.core.util.WhatsAppIntent
import com.laundry_kos.data.dto.LaporanBarisDto
import com.laundry_kos.data.dto.LaporanKartuLainnyaDto
import com.laundry_kos.data.dto.LaporanKartuPenghuniDto
import com.laundry_kos.data.dto.LaporanResponse

private val RoomPillBg = Color(0xFFF6E6E6)
private val SoftOkBarBg = Color(0xFFE9F7EF)
private val SoftOkText = Color(0xFF15803D)
private val SoftDangerBarBg = Color(0xFFFDECEC)
private val SoftEntriesBg = Color(0xFFFAF7F2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaporanScreen(
    viewModel: LaporanViewModel,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val context = LocalContext.current
    val isWide = rememberIsWideLayout()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRekapWaModal by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val laporan = uiState.data

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (laporan != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, ext.line),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Button(
                            onClick = { LaporanPdfPrinter.printLaporan(context, laporan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ext.gold,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cetak / Simpan PDF",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.refreshing,
            onRefresh = { viewModel.load(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Panel Filter Periode + Rekap WA
                item {
                    LaundryCard(contentPadding = PaddingValues(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                                if (laporan != null) {
                                    IconButton(
                                        onClick = { showRekapWaModal = true },
                                    ) {
                                        Icon(
                                            Icons.Default.Forum,
                                            contentDescription = "Rekap WA Gedung",
                                            tint = ext.whatsapp,
                                        )
                                    }
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
                                Text(
                                    text = "Tampilkan catatan entri (mode rinci)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.muted,
                                )
                                Switch(
                                    checked = uiState.rinci,
                                    onCheckedChange = viewModel::toggleRinci,
                                )
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
                        Box(modifier = Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (laporan != null) {
                    // 3 Kotak Ringkasan (persis web & PDF gambar 7)
                    item {
                        val r = laporan.ringkasan
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                Column {
                                    Text(
                                        text = laporan.rentang.label,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                    Text(
                                        text = "Kuota gratis ${laporan.kuotaGratisLabel} kg / penghuni",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.muted,
                                    )
                                }
                                Text(
                                    text = "Dicetak ${laporan.dicetakLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.muted,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LaporanStatTile(
                                    label = "TOTAL LAUNDRY",
                                    value = "${r.totalKgLabel} kg",
                                    valueColor = ext.gold,
                                    modifier = Modifier.weight(1f),
                                )
                                LaporanStatTile(
                                    label = "PENGHUNI",
                                    value = r.jumlahPenghuni.toString(),
                                    valueColor = ext.gold,
                                    modifier = Modifier.weight(1f),
                                )
                                LaporanStatTile(
                                    label = "DEKAT / LEWAT",
                                    value = r.jumlahMendekatiLewat.toString(),
                                    valueColor = ext.danger,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    if (laporan.kartuPenghuni.isEmpty() && laporan.kartuLainnya.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "Belum ada catatan laundry",
                                subtitle = "Tidak ada data laporan pada periode ${laporan.rentang.label}.",
                            )
                        }
                    } else {
                        // Portrait: 1 kolom lega (persis gambar 9) | Landscape/Tablet: 2 kolom adaptif
                        if (isWide) {
                            val pairs = laporan.kartuPenghuni.chunked(2)
                            items(pairs) { rowCards ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    rowCards.forEach { kartu ->
                                        LaporanPenghuniDetailCard(
                                            kartu = kartu,
                                            bulanAcuan = laporan.rentang.bulanAcuan.ifBlank { kartu.kuota.bulan },
                                            showCatatanEntri = uiState.rinci,
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
                        } else {
                            items(laporan.kartuPenghuni, key = { it.penghuni.id }) { kartu ->
                                LaporanPenghuniDetailCard(
                                    kartu = kartu,
                                    bulanAcuan = laporan.rentang.bulanAcuan.ifBlank { kartu.kuota.bulan },
                                    showCatatanEntri = uiState.rinci,
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
                                )
                            }
                        }

                        // Kartu Lainnya (GDG / Tanpa Nama)
                        if (laporan.kartuLainnya.isNotEmpty()) {
                            items(laporan.kartuLainnya, key = { it.jenis }) { lain ->
                                LaporanLainnyaDetailCard(kartu = lain, showCatatanEntri = uiState.rinci)
                            }
                        }

                        // Footer Total Keseluruhan Marun
                        item {
                            LaporanTotalFooter(laporan = laporan)
                        }
                    }
                }
            }
        }
    }

    if (showRekapWaModal && laporan != null) {
        RekapWaDialog(
            laporan = laporan,
            onDismiss = { showRekapWaModal = false },
        )
    }
}

@Composable
private fun LaporanStatTile(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    LaundryCard(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = LaundryKosTheme.extendedColors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = valueColor,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LaporanPenghuniDetailCard(
    kartu: LaporanKartuPenghuniDto,
    bulanAcuan: String,
    showCatatanEntri: Boolean,
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
    val kuota = kartu.kuota
    val isLewat = kuota.status == "lewat"
    val isMendekati = kuota.status == "mendekati"
    val terpakaiLbl = kuota.terpakaiLabel ?: kuota.terpakaiKg
    val kuotaLbl = kuota.kuotaLabel ?: kuota.kuotaKg
    val lebihLbl = kuota.kelebihanLabel ?: kuota.kelebihanKg

    val statusDotColor = when {
        isLewat -> ext.danger
        isMendekati -> ext.gold
        else -> SoftOkText
    }
    val statusPillBg = when {
        isLewat -> SoftDangerBarBg
        isMendekati -> ext.goldContainer
        else -> SoftOkBarBg
    }
    val statusLabel = when {
        isLewat -> "● Lewat kuota"
        isMendekati -> "● Mendekati kuota"
        else -> "● Normal"
    }
    val totalKgColor = when {
        isLewat -> ext.danger
        isMendekati -> ext.gold
        else -> MaterialTheme.colorScheme.primary
    }

    LaundryCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Kartu (persis Gambar 9)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Surface(
                        color = RoomPillBg,
                        shape = RoundedCornerShape(999.dp),
                    ) {
                        Text(
                            text = kartu.labelKamar,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = kartu.penghuni.nama,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                color = statusPillBg,
                                shape = RoundedCornerShape(999.dp),
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = statusDotColor,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = buildString {
                                append("${kartu.jumlahCatatan} catatan · Penghuni")
                                if (kartu.penghuni.status == "keluar") append(" · Keluar")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.muted,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${kartu.totalKgLabel} kg",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = totalKgColor,
                    )
                    val subRight = if (kartu.jumlahItemTerpisah > 0) {
                        "${kartu.jumlahCatatan} catatan · ${kartu.jumlahItemTerpisah} item"
                    } else {
                        "${kartu.jumlahCatatan} catatan"
                    }
                    Text(
                        text = subRight,
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.muted,
                    )
                }
            }

            // Baris Ringkas Kuota Bulan Ini
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$terpakaiLbl / $kuotaLbl kg kuota gratis bulan ini",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.muted,
                    modifier = Modifier.weight(1f),
                )
                if (kuota.kelebihanCenti > 0) {
                    Text(
                        text = "Lewat bulan ini $lebihLbl kg",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ext.danger,
                    )
                }
            }

            // Daftar Rincian Bergaris Titik-Titik (SELALU tampil persis Gambar 9)
            DottedEntriesBox(
                baris = kartu.baris,
                subtotalKgLabel = kartu.totalKgLabel,
                showCatatanEntri = showCatatanEntri,
            )

            // Batang Ringkasan Biaya & Aksi Berwarna (persis Gambar 9 + Web)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Biaya Utama
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftOkBarBg,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Biaya utama",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SoftOkText,
                        )
                        Text(
                            text = Formatters.formatRupiah(kartu.biayaUtama),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = SoftOkText,
                        )
                    }
                }

                // Tagihan Lewat Kuota + Inline Edit (persis Gambar 9)
                val punyaKelebihan = kuota.kelebihanCenti > 0
                val adaTagihanLewat = kartu.tagihanLewatKuota > 0
                val sudahDisesuaikan = kuota.nominalPenyesuaian != null
                if (punyaKelebihan || adaTagihanLewat || sudahDisesuaikan) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftDangerBarBg,
                        border = if (isEditing) BorderStroke(1.dp, ext.danger.copy(alpha = 0.4f)) else null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isEditing) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = ext.danger, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Tagihan lewat kuota $lebihLbl kg ($bulanAcuan)",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ext.danger,
                                        )
                                    }
                                    if (sudahDisesuaikan) {
                                        Text(
                                            text = "Reset otomatis",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable(enabled = !inlineSaving) {
                                                onSaveInline(true)
                                            },
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = inlineNominal,
                                        onValueChange = onInlineNominalChange,
                                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = ext.danger) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                    )
                                    Button(
                                        onClick = { onSaveInline(false) },
                                        enabled = !inlineSaving && inlineNominal.toIntOrNull() != null,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ext.danger),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                    ) {
                                        Text("OK", fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = onCancelInline,
                                        enabled = !inlineSaving,
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Batal", tint = ext.danger)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = kartu.bolehSesuaikanKuota) { onStartInlineEdit() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = ext.danger,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Tagihan lewat kuota $lebihLbl kg" +
                                            if (sudahDisesuaikan) " (disesuaikan)" else "",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = ext.danger,
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = Formatters.formatRupiah(kartu.tagihanLewatKuota),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = ext.danger,
                                    )
                                    if (kartu.bolehSesuaikanKuota) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit tagihan lewat kuota",
                                            tint = ext.danger,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bayar Terpisah (persis Gambar 9)
                if (kartu.bayarTerpisah > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ext.goldContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = ext.gold,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Bayar terpisah ${kartu.jumlahItemTerpisah} item",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = ext.onGold,
                                )
                            }
                            Text(
                                text = Formatters.formatRupiah(kartu.bayarTerpisah),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = ext.onGold,
                            )
                        }
                    }
                }

                // Kirim Tagihan via WhatsApp (persis Gambar 9)
                val totalTagihanWa = kartu.waTagihan?.totalTagihan ?: 0
                if (totalTagihanWa > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftOkBarBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSendWa),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = null,
                                    tint = ext.whatsapp,
                                    modifier = Modifier.size(20.dp),
                                )
                                Column {
                                    Text(
                                        text = "Kirim tagihan via WhatsApp",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = SoftOkText,
                                    )
                                    Text(
                                        text = "Buka chat langsung ke ${kartu.penghuni.nama}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SoftOkText.copy(alpha = 0.85f),
                                    )
                                }
                            }
                            Text(
                                text = "WA",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = SoftOkText,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DottedEntriesBox(
    baris: List<LaporanBarisDto>,
    subtotalKgLabel: String,
    showCatatanEntri: Boolean,
) {
    val ext = LaundryKosTheme.extendedColors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SoftEntriesBg,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            baris.forEach { row ->
                val left = if (row.tipe == "item") {
                    "${row.tanggalLabel} - ${row.namaItem ?: "Item"}"
                } else {
                    row.tanggalLabel
                }
                val right = if (row.tipe == "item") {
                    "1 item - ${Formatters.formatRupiah(row.biaya, "Rp0")}"
                } else {
                    val beratPart = "${row.beratLabel ?: "-"} kg"
                    if (row.biaya != null) "$beratPart - ${Formatters.formatRupiah(row.biaya)}" else beratPart
                }
                DottedLeaderRow(
                    leftText = left,
                    rightText = right,
                    rightColor = if (row.merah) ext.danger else MaterialTheme.colorScheme.onSurface,
                )
                if (showCatatanEntri && !row.catatan.isNullOrBlank()) {
                    Text(
                        text = "Catatan: ${row.catatan}",
                        style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                        color = ext.muted,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }

            HorizontalDivider(
                color = ext.line,
                modifier = Modifier.padding(vertical = 2.dp),
            )

            DottedLeaderRow(
                leftText = "Subtotal semua",
                rightText = "$subtotalKgLabel kg",
                leftColor = MaterialTheme.colorScheme.onSurface,
                rightColor = MaterialTheme.colorScheme.onSurface,
                leftStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                rightStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
            )
        }
    }
}

@Composable
private fun LaporanLainnyaDetailCard(
    kartu: LaporanKartuLainnyaDto,
    showCatatanEntri: Boolean,
) {
    val ext = LaundryKosTheme.extendedColors
    LaundryCard(contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(kartu.judul, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text("${kartu.jumlahCatatan} catatan", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                }
                Text(
                    text = "${kartu.totalKgLabel} kg",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            DottedEntriesBox(
                baris = kartu.baris,
                subtotalKgLabel = kartu.totalKgLabel,
                showCatatanEntri = showCatatanEntri,
            )

            Surface(shape = RoundedCornerShape(12.dp), color = SoftOkBarBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Biaya utama", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SoftOkText)
                    Text(Formatters.formatRupiah(kartu.biayaUtama), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold), color = SoftOkText)
                }
            }

            if (kartu.bayarTerpisah > 0) {
                Surface(shape = RoundedCornerShape(12.dp), color = ext.goldContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Bayar terpisah ${kartu.jumlahItemTerpisah} item", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ext.onGold)
                        Text(Formatters.formatRupiah(kartu.bayarTerpisah), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold), color = ext.onGold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LaporanTotalFooter(laporan: LaporanResponse) {
    val ext = LaundryKosTheme.extendedColors
    val r = laporan.ringkasan
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total keseluruhan laundry",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Text(
                        text = "Lewat kuota ${Formatters.formatRupiah(r.totalTagihanLewat)} · Utama ${Formatters.formatRupiah(r.totalBiayaUtama)} · Terpisah ${Formatters.formatRupiah(r.totalBayarTerpisah)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${r.totalKgLabel} kg",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                )
            }
            Surface(color = ext.sunken, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Entri mengikuti filter tanggal. Kuota gratis dihitung dari total bulan terkait.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun RekapWaDialog(
    laporan: LaporanResponse,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val ext = LaundryKosTheme.extendedColors
    val rekap = laporan.rekapWa

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rekap Laundry Gedung (WA)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Panjang teks: ${rekap.jumlahKarakter} karakter" +
                        if (rekap.terlaluPanjang) " (gunakan Salin / Bagikan)" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.muted,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ext.surfaceAlt,
                    border = BorderStroke(1.dp, ext.line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                ) {
                    Text(
                        text = rekap.pesan,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        WhatsAppIntent.copyToClipboard(context, rekap.pesan, "Rekap Laundry")
                        onDismiss()
                    },
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin")
                }
                if (!rekap.url.isNullOrBlank()) {
                    Button(
                        onClick = {
                            WhatsAppIntent.openUrlOrCopy(context, rekap.url, rekap.pesan, "Rekap WA")
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ext.whatsapp),
                    ) {
                        Text("Buka WA")
                    }
                } else {
                    Button(
                        onClick = {
                            WhatsAppIntent.sharePlainText(context, rekap.pesan, "Rekap Laundry ${laporan.rentang.label}")
                            onDismiss()
                        },
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
    )
}

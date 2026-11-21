package com.laundry_kos.feature.penghuni

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.R
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.KuotaProgressBar
import com.laundry_kos.core.designsystem.KuotaVisualStatus
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.MonthSelectorBar
import com.laundry_kos.core.designsystem.SearchableDropdownField
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.WhatsAppIntent
import com.laundry_kos.data.dto.KamarDto
import com.laundry_kos.data.dto.UbahPenghuniRequestDto

private data class PenghuniActionItem(
    val label: String,
    val icon: ImageVector,
    val isDanger: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenghuniDetailScreen(
    viewModel: PenghuniDetailViewModel,
    onBack: () -> Unit,
    onEditLaundry: (Int) -> Unit,
    onMutated: () -> Unit = {},
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showGantiNomorDialog by remember { mutableStateOf(false) }
    var showPindahDialog by remember { mutableStateOf(false) }
    var showKeluarDialog by remember { mutableStateOf(false) }
    var showSesuaikanKuotaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    LaunchedEffect(uiState.hasMutated) {
        if (uiState.hasMutated) {
            onMutated()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.data?.penghuni?.nama ?: "Detail Penghuni",
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        if (uiState.loading && uiState.data == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val data = uiState.data
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonthSelectorBar(
                    bulanYm = uiState.bulan,
                    onChangeBulan = viewModel::changeBulan,
                )
            }

            if (uiState.error != null) {
                item {
                    ErrorBannerCard(message = uiState.error!!, onRetry = { viewModel.load() })
                }
            }

            if (data != null) {
                val p = data.penghuni
                val perms = data.permissions
                val kuota = data.kuotaBulanIni

                // Profil & Aksi Cepat
                item {
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.nama + (p.alias?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = "Kamar ${p.kamarNomor ?: "-"} · ${p.jangkaWaktuLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.muted,
                                    )
                                }
                                StatusBadge(
                                    text = p.statusLabel.ifBlank { p.status },
                                    containerColor = if (p.status == "aktif") ext.okContainer else ext.sunken,
                                    contentColor = if (p.status == "aktif") ext.ok else ext.muted,
                                )
                            }

                            Text("WhatsApp: ${p.noWa ?: "-"}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "Masuk: ${DateUtil.formatIsoDateLabel(p.tanggalMasuk)}" +
                                    (p.tanggalKeluar?.let { " · Keluar: ${DateUtil.formatIsoDateLabel(it)}" } ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.muted,
                            )
                            if (!p.catatan.isNullOrBlank()) {
                                Text("Catatan: ${p.catatan}", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                            }

                            // Tombol WA (seimbang full-width di portrait & landscape)
                            val waTagihanValid = data.waTagihan?.takeIf { (it.totalTagihan ?: 0) > 0 }
                            val waPeringatanValid = data.waPeringatan
                            if (waTagihanValid != null || waPeringatanValid != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    waTagihanValid?.let { wa ->
                                        Button(
                                            onClick = {
                                                WhatsAppIntent.openUrlOrCopy(context, wa.url, wa.pesan, "WA Tagihan")
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ext.whatsapp,
                                                contentColor = Color.White,
                                            ),
                                        ) {
                                            Icon(painterResource(R.drawable.ic_whatsapp), contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "WA Tagihan (${Formatters.formatRupiah(wa.totalTagihan)})",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    waPeringatanValid?.let { wa ->
                                        OutlinedButton(
                                            onClick = {
                                                WhatsAppIntent.openUrlOrCopy(context, wa.url, wa.pesan, "WA Peringatan")
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(42.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, ext.lineStrong),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                        ) {
                                            Text(
                                                text = "WA Peringatan",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                            }

                            // Tombol Aksi Kontrak & Nomor (grid rapi 2 kolom portrait / 4 kolom landscape)
                            val actionItems = buildList {
                                if (perms.bolehKelolaPenghuni) {
                                    add(
                                        PenghuniActionItem("Edit", Icons.Default.Edit) {
                                            viewModel.clearDialogError()
                                            showEditDialog = true
                                        },
                                    )
                                    add(
                                        PenghuniActionItem("Ganti WA", Icons.Default.Phone) {
                                            viewModel.clearDialogError()
                                            showGantiNomorDialog = true
                                        },
                                    )
                                }
                                if (perms.bolehKelolaKontrak && p.status != "keluar") {
                                    add(
                                        PenghuniActionItem("Pindah Kamar", Icons.Default.SwapHoriz) {
                                            viewModel.clearDialogError()
                                            viewModel.loadKamarKosong()
                                            showPindahDialog = true
                                        },
                                    )
                                    add(
                                        PenghuniActionItem("Keluar", Icons.AutoMirrored.Filled.ExitToApp, isDanger = true) {
                                            viewModel.clearDialogError()
                                            showKeluarDialog = true
                                        },
                                    )
                                }
                                if (perms.bisaBatalkanPindah) {
                                    add(PenghuniActionItem("Batal Pindah", Icons.AutoMirrored.Filled.Undo) { viewModel.batalkanPindah() })
                                }
                                if (perms.bolehKelolaKontrak && p.status == "keluar") {
                                    add(PenghuniActionItem("Batalkan Keluar", Icons.AutoMirrored.Filled.Undo) { viewModel.batalkanKeluar() })
                                }
                            }
                            if (actionItems.isNotEmpty()) {
                                val columns = if (isWide) 4 else 2
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    actionItems.chunked(columns).forEach { rowBtns ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            rowBtns.forEach { btn ->
                                                val fg = if (btn.isDanger) ext.danger else MaterialTheme.colorScheme.primary
                                                val border = if (btn.isDanger) ext.danger.copy(alpha = 0.45f) else ext.lineStrong
                                                OutlinedButton(
                                                    onClick = btn.onClick,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(40.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, border),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = fg),
                                                ) {
                                                    Icon(btn.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = btn.label,
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                            if (isWide && rowBtns.size < columns) {
                                                repeat(columns - rowBtns.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Kuota Bulan Ini
                item {
                    val visual = KuotaVisualStatus.fromApi(kuota.status)
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Kuota ${kuota.labelBulan}", style = MaterialTheme.typography.titleSmall)
                                if (perms.bolehSesuaikanKuota) {
                                    TextButton(
                                        onClick = {
                                            viewModel.clearDialogError()
                                            showSesuaikanKuotaDialog = true
                                        },
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sesuaikan")
                                    }
                                }
                            }
                            if (kuota.modeKuota == "tanpa_kuota") {
                                Text("Mode tanpa kuota aktif.", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                            } else {
                                KuotaProgressBar(persen = kuota.persenBar, status = visual)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("${kuota.terpakaiKg} / ${kuota.kuotaKg} kg (${kuota.persen}%)", style = MaterialTheme.typography.bodySmall)
                                    Text("Sisa: ${kuota.sisaKg} kg", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Tagihan lewat kuota", style = MaterialTheme.typography.bodySmall)
                                    val manualTag = if (kuota.nominalPenyesuaian != null) " (manual)" else ""
                                    Text(
                                        text = Formatters.formatRupiah(kuota.tagihanLewatKuota) + manualTag,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (kuota.tagihanLewatKuota > 0) ext.danger else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Bayar terpisah", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = Formatters.formatRupiah(kuota.bayarTerpisah),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            }
                        }
                    }
                }

                // Riwayat Kuota 6 Bulan
                if (data.riwayatKuota.isNotEmpty()) {
                    item {
                        LaundryCard(containerColor = ext.surfaceAlt) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Riwayat Kuota 6 Bulan", style = MaterialTheme.typography.titleSmall)
                                data.riwayatKuota.forEach { r ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(r.labelBulan.ifBlank { r.bulan }, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            text = "${r.terpakaiKg}/${r.kuotaKg} kg · ${Formatters.formatRupiah(r.totalTagihan)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (r.status == "lewat") ext.danger else ext.muted,
                                        )
                                    }
                                    HorizontalDivider(color = ext.line)
                                }
                            }
                        }
                    }
                }

                // Riwayat Penempatan Kamar
                if (data.penempatan.isNotEmpty()) {
                    item {
                        LaundryCard {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Riwayat Penempatan Kamar", style = MaterialTheme.typography.titleSmall)
                                data.penempatan.forEach { pen ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = "Kamar ${pen.kamarNomor ?: "-"} (${pen.sebab ?: "-"})",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        )
                                        Text(
                                            text = "${DateUtil.formatIsoDateLabel(pen.mulai)} – ${pen.selesai?.let(DateUtil::formatIsoDateLabel) ?: "Sekarang"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.muted,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Catatan Laundry Terakhir
                item {
                    Text(
                        text = "Catatan Laundry Terakhir (${data.catatanLaundry.size})",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(data.catatanLaundry, key = { it.id }) { l ->
                    LaundryCard(
                        modifier = Modifier.clickable(enabled = l.bolehUbah) { onEditLaundry(l.id) },
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(DateUtil.formatIsoDateLabel(l.tanggal), style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Kamar ${l.kamarNomor ?: "-"} · ${Formatters.formatBeratKg(l.beratCenti, l.beratLabel)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.muted,
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatRupiah(l.biayaUtama),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                if (l.itemBiaya > 0) {
                                    Text(
                                        text = "+Terpisah ${Formatters.formatRupiah(l.itemBiaya)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.onGold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }

    val detail = uiState.data
    if (detail != null) {
        val p = detail.penghuni

        if (showEditDialog) {
            var nama by remember { mutableStateOf(p.nama) }
            var alias by remember { mutableStateOf(p.alias ?: "") }
            var status by remember { mutableStateOf(p.status) }
            var tanggalMasuk by remember { mutableStateOf(p.tanggalMasuk ?: DateUtil.todayIso()) }
            var jangka by remember { mutableStateOf(p.jangkaWaktuSewa) }
            var catatan by remember { mutableStateOf(p.catatan ?: "") }
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.actionLoading) {
                        viewModel.clearDialogError()
                        showEditDialog = false
                    }
                },
                title = { Text("Edit Data Penghuni") },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (!uiState.dialogError.isNullOrBlank()) {
                            ErrorBannerCard(message = uiState.dialogError!!)
                        }
                        OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Alias") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        if (detail.permissions.bolehKelolaKontrak) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("aktif" to "Aktif", "booking" to "Booking").forEach { (k, l) ->
                                    FilterChip(selected = status == k, onClick = { status = k }, label = { Text(l) })
                                }
                            }
                            DatePickerField(label = "Tanggal Masuk", valueIso = tanggalMasuk, onValueSelected = { tanggalMasuk = it })
                        }
                        OutlinedTextField(value = catatan, onValueChange = { catatan = it }, label = { Text("Catatan") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.actionLoading && nama.isNotBlank(),
                        onClick = {
                            viewModel.ubahData(
                                UbahPenghuniRequestDto(
                                    nama = nama.trim(),
                                    alias = alias.trim().takeIf { it.isNotEmpty() },
                                    status = status,
                                    tanggalMasuk = tanggalMasuk,
                                    jangkaWaktuSewa = jangka,
                                    catatan = catatan.trim().takeIf { it.isNotEmpty() },
                                ),
                            ) { showEditDialog = false }
                        },
                    ) { Text(if (uiState.actionLoading) "Menyimpan..." else "Simpan") }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.clearDialogError()
                            showEditDialog = false
                        },
                    ) { Text("Batal") }
                },
            )
        }

        if (showGantiNomorDialog) {
            var noWa by remember { mutableStateOf(p.noWa ?: "") }
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.actionLoading) {
                        viewModel.clearDialogError()
                        showGantiNomorDialog = false
                    }
                },
                title = { Text("Ganti Nomor WhatsApp") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!uiState.dialogError.isNullOrBlank()) {
                            ErrorBannerCard(message = uiState.dialogError!!)
                        }
                        OutlinedTextField(
                            value = noWa,
                            onValueChange = { noWa = it },
                            label = { Text("Nomor WhatsApp") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.gantiNomor(noWa.trim()) { showGantiNomorDialog = false }
                        },
                    ) { Text(if (uiState.actionLoading) "Menyimpan..." else "Simpan") }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.clearDialogError()
                            showGantiNomorDialog = false
                        },
                    ) { Text("Batal") }
                },
            )
        }

        if (showPindahDialog) {
            var selectedKamar by remember { mutableStateOf<KamarDto?>(null) }
            var tglPindah by remember { mutableStateOf(DateUtil.todayIso()) }
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.actionLoading) {
                        viewModel.clearDialogError()
                        showPindahDialog = false
                    }
                },
                title = { Text("Pindah Kamar") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!uiState.dialogError.isNullOrBlank()) {
                            ErrorBannerCard(message = uiState.dialogError!!)
                        }
                        Text("Kuota bulan berjalan tetap utuh setelah pindah kamar.", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                        SearchableDropdownField(
                            label = "Kamar Tujuan Kosong *",
                            items = uiState.kamarKosong,
                            selectedItem = selectedKamar,
                            itemLabel = { k -> "Kamar ${k.nomor} (Lt. ${k.lantai})" },
                            onSelect = { selectedKamar = it },
                            placeholder = "Ketik nomor kamar kosong...",
                            emptyText = "Tidak ada kamar kosong.",
                            matchPredicate = { k, q ->
                                k.nomor.contains(q, ignoreCase = true) ||
                                    k.lantai.toString().contains(q, ignoreCase = true)
                            },
                        )
                        DatePickerField(label = "Tanggal Pindah", valueIso = tglPindah, onValueSelected = { tglPindah = it })
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.actionLoading && selectedKamar != null,
                        onClick = {
                            val tujuan = selectedKamar?.id ?: return@TextButton
                            viewModel.pindahKamar(tujuan, tglPindah) { showPindahDialog = false }
                        },
                    ) { Text(if (uiState.actionLoading) "Memindahkan..." else "Pindahkan") }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.clearDialogError()
                            showPindahDialog = false
                        },
                    ) { Text("Batal") }
                },
            )
        }

        if (showKeluarDialog) {
            var tglKeluar by remember { mutableStateOf(DateUtil.todayIso()) }
            var alasan by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.actionLoading) {
                        viewModel.clearDialogError()
                        showKeluarDialog = false
                    }
                },
                title = { Text("Catat Penghuni Keluar") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!uiState.dialogError.isNullOrBlank()) {
                            ErrorBannerCard(message = uiState.dialogError!!)
                        }
                        DatePickerField(label = "Tanggal Keluar", valueIso = tglKeluar, onValueSelected = { tglKeluar = it })
                        OutlinedTextField(value = alasan, onValueChange = { alasan = it }, label = { Text("Alasan Keluar (opsional)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.keluar(tglKeluar, alasan.trim()) { showKeluarDialog = false }
                        },
                    ) { Text(if (uiState.actionLoading) "Menyimpan..." else "Catat Keluar", color = ext.danger) }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.clearDialogError()
                            showKeluarDialog = false
                        },
                    ) { Text("Batal") }
                },
            )
        }

        if (showSesuaikanKuotaDialog) {
            val kuota = detail.kuotaBulanIni
            var nominal by remember { mutableStateOf((kuota.nominalPenyesuaian ?: kuota.tagihanLewatKuota).toString()) }
            var catatan by remember { mutableStateOf(kuota.catatanPenyesuaian ?: "") }
            AlertDialog(
                onDismissRequest = {
                    if (!uiState.actionLoading) {
                        viewModel.clearDialogError()
                        showSesuaikanKuotaDialog = false
                    }
                },
                title = { Text("Sesuaikan Tagihan Lewat Kuota (${kuota.labelBulan})") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!uiState.dialogError.isNullOrBlank()) {
                            ErrorBannerCard(message = uiState.dialogError!!)
                        }
                        Text(
                            text = "Hitungan otomatis: ${Formatters.formatRupiah(kuota.tagihanOtomatis)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.muted,
                        )
                        OutlinedTextField(
                            value = nominal,
                            onValueChange = { nominal = it.filter(Char::isDigit) },
                            label = { Text("Nominal Manual (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = catatan,
                            onValueChange = { catatan = it },
                            label = { Text("Catatan penyesuaian (opsional)") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (kuota.nominalPenyesuaian != null) {
                            TextButton(
                                enabled = !uiState.actionLoading,
                                onClick = {
                                    viewModel.simpanPenyesuaianKuota(null, reset = true, catatan = null) {
                                        showSesuaikanKuotaDialog = false
                                    }
                                },
                            ) { Text("Reset Otomatis") }
                        }
                        TextButton(
                            enabled = !uiState.actionLoading && nominal.toIntOrNull() != null,
                            onClick = {
                                viewModel.simpanPenyesuaianKuota(nominal.toIntOrNull(), reset = false, catatan = catatan.trim()) {
                                    showSesuaikanKuotaDialog = false
                                }
                            },
                        ) { Text(if (uiState.actionLoading) "Menyimpan..." else "Simpan") }
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !uiState.actionLoading,
                        onClick = {
                            viewModel.clearDialogError()
                            showSesuaikanKuotaDialog = false
                        },
                    ) { Text("Batal") }
                },
            )
        }
    }
}

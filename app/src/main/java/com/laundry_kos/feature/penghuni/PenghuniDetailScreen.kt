package com.laundry_kos.feature.penghuni

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.WhatsAppIntent
import com.laundry_kos.data.dto.KamarDto
import com.laundry_kos.data.dto.UbahPenghuniRequestDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PenghuniDetailScreen(
    viewModel: PenghuniDetailViewModel,
    onBack: () -> Unit,
    onEditLaundry: (Int) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
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

                            // Tombol WA
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                data.waTagihan?.let { wa ->
                                    if ((wa.totalTagihan ?: 0) > 0) {
                                        Button(
                                            onClick = {
                                                WhatsAppIntent.openUrlOrCopy(context, wa.url, wa.pesan, "WA Tagihan")
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ext.whatsapp),
                                        ) {
                                            Icon(painterResource(R.drawable.ic_whatsapp), contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("WA Tagihan (${Formatters.formatRupiah(wa.totalTagihan)})", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                                data.waPeringatan?.let { wa ->
                                    OutlinedButton(
                                        onClick = {
                                            WhatsAppIntent.openUrlOrCopy(context, wa.url, wa.pesan, "WA Peringatan")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Text("WA Peringatan", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            // Tombol Aksi Kontrak & Nomor
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (perms.bolehKelolaPenghuni) {
                                    OutlinedButton(onClick = { showEditDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit")
                                    }
                                    OutlinedButton(onClick = { showGantiNomorDialog = true }) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ganti WA")
                                    }
                                }
                                if (perms.bolehKelolaKontrak && p.status != "keluar") {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.loadKamarKosong()
                                            showPindahDialog = true
                                        },
                                    ) {
                                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pindah Kamar")
                                    }
                                    OutlinedButton(onClick = { showKeluarDialog = true }) {
                                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Keluar", color = ext.danger)
                                    }
                                }
                                if (perms.bisaBatalkanPindah) {
                                    OutlinedButton(onClick = viewModel::batalkanPindah) {
                                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Batal Pindah")
                                    }
                                }
                                if (perms.bolehKelolaKontrak && p.status == "keluar") {
                                    OutlinedButton(onClick = viewModel::batalkanKeluar) {
                                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Batalkan Keluar")
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
                                    TextButton(onClick = { showSesuaikanKuotaDialog = true }) {
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
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Data Penghuni") },
                text = {
                    Column(
                        modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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
                        enabled = nama.isNotBlank(),
                        onClick = {
                            showEditDialog = false
                            viewModel.ubahData(
                                UbahPenghuniRequestDto(
                                    nama = nama.trim(),
                                    alias = alias.trim().takeIf { it.isNotEmpty() },
                                    status = status,
                                    tanggalMasuk = tanggalMasuk,
                                    jangkaWaktuSewa = jangka,
                                    catatan = catatan.trim().takeIf { it.isNotEmpty() },
                                ),
                            )
                        },
                    ) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Batal") } },
            )
        }

        if (showGantiNomorDialog) {
            var noWa by remember { mutableStateOf(p.noWa ?: "") }
            AlertDialog(
                onDismissRequest = { showGantiNomorDialog = false },
                title = { Text("Ganti Nomor WhatsApp") },
                text = {
                    OutlinedTextField(
                        value = noWa,
                        onValueChange = { noWa = it },
                        label = { Text("Nomor WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showGantiNomorDialog = false
                        viewModel.gantiNomor(noWa.trim())
                    }) { Text("Simpan") }
                },
                dismissButton = { TextButton(onClick = { showGantiNomorDialog = false }) { Text("Batal") } },
            )
        }

        if (showPindahDialog) {
            var selectedKamar by remember { mutableStateOf<KamarDto?>(null) }
            var expanded by remember { mutableStateOf(false) }
            var tglPindah by remember { mutableStateOf(DateUtil.todayIso()) }
            AlertDialog(
                onDismissRequest = { showPindahDialog = false },
                title = { Text("Pindah Kamar") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kuota bulan berjalan tetap utuh setelah pindah kamar.", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                        Box {
                            OutlinedTextField(
                                value = selectedKamar?.let { "Kamar ${it.nomor} (Lt. ${it.lantai})" } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Kamar Tujuan Kosong *") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                uiState.kamarKosong.forEach { k ->
                                    DropdownMenuItem(
                                        text = { Text("Kamar ${k.nomor} (Lt. ${k.lantai})") },
                                        onClick = {
                                            selectedKamar = k
                                            expanded = false
                                        },
                                    )
                                }
                            }
                        }
                        DatePickerField(label = "Tanggal Pindah", valueIso = tglPindah, onValueSelected = { tglPindah = it })
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = selectedKamar != null,
                        onClick = {
                            val tujuan = selectedKamar?.id ?: return@TextButton
                            showPindahDialog = false
                            viewModel.pindahKamar(tujuan, tglPindah)
                        },
                    ) { Text("Pindahkan") }
                },
                dismissButton = { TextButton(onClick = { showPindahDialog = false }) { Text("Batal") } },
            )
        }

        if (showKeluarDialog) {
            var tglKeluar by remember { mutableStateOf(DateUtil.todayIso()) }
            var alasan by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showKeluarDialog = false },
                title = { Text("Catat Penghuni Keluar") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DatePickerField(label = "Tanggal Keluar", valueIso = tglKeluar, onValueSelected = { tglKeluar = it })
                        OutlinedTextField(value = alasan, onValueChange = { alasan = it }, label = { Text("Alasan Keluar (opsional)") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showKeluarDialog = false
                        viewModel.keluar(tglKeluar, alasan.trim())
                    }) { Text("Catat Keluar", color = ext.danger) }
                },
                dismissButton = { TextButton(onClick = { showKeluarDialog = false }) { Text("Batal") } },
            )
        }

        if (showSesuaikanKuotaDialog) {
            val kuota = detail.kuotaBulanIni
            var nominal by remember { mutableStateOf((kuota.nominalPenyesuaian ?: kuota.tagihanLewatKuota).toString()) }
            var catatan by remember { mutableStateOf(kuota.catatanPenyesuaian ?: "") }
            AlertDialog(
                onDismissRequest = { showSesuaikanKuotaDialog = false },
                title = { Text("Sesuaikan Tagihan Lewat Kuota (${kuota.labelBulan})") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            TextButton(onClick = {
                                showSesuaikanKuotaDialog = false
                                viewModel.simpanPenyesuaianKuota(null, reset = true, catatan = null)
                            }) { Text("Reset Otomatis") }
                        }
                        TextButton(
                            enabled = nominal.toIntOrNull() != null,
                            onClick = {
                                showSesuaikanKuotaDialog = false
                                viewModel.simpanPenyesuaianKuota(nominal.toIntOrNull(), reset = false, catatan = catatan.trim())
                            },
                        ) { Text("Simpan") }
                    }
                },
                dismissButton = { TextButton(onClick = { showSesuaikanKuotaDialog = false }) { Text("Batal") } },
            )
        }
    }
}

package com.laundry_kos.feature.laundry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.LoadMoreButton
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.data.dto.LaundryDto
import com.laundry_kos.data.dto.PermissionsDto

private val PERIODE_OPTIONS = listOf(
    "bulan_ini" to "Bulan Ini",
    "bulan_lalu" to "Bulan Lalu",
    "hari_ini" to "Hari Ini",
    "7_hari" to "7 Hari",
    "semua" to "Semua",
    "rentang" to "Rentang",
)

private val JENIS_OPTIONS = listOf(
    "" to "Semua Jenis",
    "penghuni" to "Penghuni",
    "gedung" to "Gedung",
    "tanpa_nama" to "Tanpa Nama",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryListScreen(
    viewModel: LaundryListViewModel,
    permissions: PermissionsDto,
    onAddLaundry: () -> Unit,
    onEditLaundry: (Int) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteConfirmTarget by remember { mutableStateOf<LaundryDto?>(null) }
    var showLengkapiConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (permissions.catatLaundry) {
                FloatingActionButton(
                    onClick = onAddLaundry,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Catat Laundry")
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.cari,
                        onValueChange = viewModel::setCari,
                        placeholder = { Text("Cari nama, kamar, item, catatan...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PERIODE_OPTIONS.forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.periode == key,
                                onClick = { viewModel.setPeriode(key) },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                if (uiState.periode == "rentang") {
                    item {
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
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        JENIS_OPTIONS.forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.jenis == key,
                                onClick = { viewModel.setJenis(key) },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                val data = uiState.data
                if (data != null) {
                    item {
                        val r = data.ringkasan
                        LaundryCard(containerColor = ext.surfaceAlt, contentPadding = PaddingValues(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "${data.rentang.label} · ${r.jumlahCatatan} catatan",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = "${r.totalBeratKg} kg",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "Utama: ${Formatters.formatRupiah(r.totalBiayaUtama)}",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        text = "Terpisah: ${Formatters.formatRupiah(r.totalBayarTerpisah)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.onGold,
                                    )
                                }
                            }
                        }
                    }

                    if ((permissions.lengkapiHarga && data.pratinjauLengkapiHarga.jumlah > 0) ||
                        (permissions.lihatTerhapus && data.jumlahTerhapus > 0)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (permissions.lengkapiHarga && data.pratinjauLengkapiHarga.jumlah > 0) {
                                    OutlinedButton(
                                        onClick = { showLengkapiConfirm = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, ext.gold),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.onGold),
                                    ) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Lengkapi Harga (${data.pratinjauLengkapiHarga.jumlah})", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                if (permissions.lihatTerhapus && data.jumlahTerhapus > 0) {
                                    OutlinedButton(
                                        onClick = viewModel::openTerhapusDialog,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                    ) {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Terhapus (${data.jumlahTerhapus})", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        ErrorBannerCard(
                            message = uiState.error!!,
                            onRetry = { viewModel.load() },
                        )
                    }
                }

                if (uiState.loading && data == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (data != null && data.data.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada catatan laundry",
                            subtitle = "Ubah filter periode atau tekan tombol + untuk mencatat laundry baru.",
                        )
                    }
                } else if (data != null) {
                    if (isWide) {
                        items(data.data.chunked(2)) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                pair.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        LaundryRowCard(
                                            item = item,
                                            onEdit = { onEditLaundry(item.id) },
                                            onDelete = { deleteConfirmTarget = item },
                                            onTetapkanPemilik = { viewModel.openTetapkanPemilik(item) },
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        items(data.data, key = { it.id }) { item ->
                            LaundryRowCard(
                                item = item,
                                onEdit = { onEditLaundry(item.id) },
                                onDelete = { deleteConfirmTarget = item },
                                onTetapkanPemilik = { viewModel.openTetapkanPemilik(item) },
                            )
                        }
                    }

                    if (data.meta.adaLagi) {
                        item {
                            LoadMoreButton(
                                ditampilkan = data.meta.ditampilkan,
                                total = data.meta.total,
                                loading = uiState.loadingMore,
                                onClick = viewModel::loadMore,
                            )
                        }
                    }
                }
            }
        }
    }

    // Konfirmasi Hapus
    deleteConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Hapus Catatan Laundry?") },
            text = {
                Text(
                    "Catatan tanggal ${DateUtil.formatIsoDateLabel(target.tanggal)} (${target.penghuniNama ?: target.jenisLabel}) akan dihapus lunak dan bisa dipulihkan oleh pengelola.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hapusLaundry(target.id)
                        deleteConfirmTarget = null
                    },
                ) {
                    Text("Hapus", color = ext.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTarget = null }) {
                    Text("Batal")
                }
            },
        )
    }

    // Konfirmasi Lengkapi Harga
    if (showLengkapiConfirm) {
        val preview = uiState.data?.pratinjauLengkapiHarga
        AlertDialog(
            onDismissRequest = { showLengkapiConfirm = false },
            title = { Text("Lengkapi Harga Otomatis") },
            text = {
                Text(
                    "Sistem akan menghitung dan mengisi biaya utama otomatis untuk ${preview?.jumlah ?: 0} catatan berberat tanpa harga (total estimasi ${Formatters.formatRupiah(preview?.total ?: 0)}).",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLengkapiConfirm = false
                        viewModel.lengkapiHarga()
                    },
                ) {
                    Text("Lengkapi Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLengkapiConfirm = false }) {
                    Text("Batal")
                }
            },
        )
    }

    // Dialog Catatan Terhapus
    if (uiState.showTerhapusDialog) {
        AlertDialog(
            onDismissRequest = viewModel::closeTerhapusDialog,
            title = { Text("Catatan Laundry Terhapus") },
            text = {
                val terhapus = uiState.terhapusData
                if (uiState.terhapusLoading && terhapus == null) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (terhapus == null || terhapus.data.isEmpty()) {
                    Text("Tidak ada catatan terhapus.")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(terhapus.data, key = { it.id }) { row ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ext.surfaceAlt,
                                border = BorderStroke(1.dp, ext.line),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = row.penghuniNama ?: row.jenisLabel,
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = "${DateUtil.formatIsoDateLabel(row.tanggal)} · ${Formatters.formatBeratKg(row.beratCenti, row.beratLabel)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.muted,
                                        )
                                    }
                                    TextButton(onClick = { viewModel.pulihkan(row.id) }) {
                                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pulihkan")
                                    }
                                }
                            }
                        }
                        if (terhapus.meta.adaLagi) {
                            item {
                                LoadMoreButton(
                                    ditampilkan = terhapus.meta.ditampilkan,
                                    total = terhapus.meta.total,
                                    loading = uiState.terhapusLoading,
                                    onClick = viewModel::loadMoreTerhapus,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::closeTerhapusDialog) {
                    Text("Tutup")
                }
            },
        )
    }

    // Dialog Tetapkan Pemilik (untuk catatan tanpa_nama)
    uiState.tetapkanTarget?.let { target ->
        var filterNama by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::dismissTetapkanPemilik,
            title = { Text("Tetapkan Pemilik (${DateUtil.formatIsoDateLabel(target.tanggal)})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = filterNama,
                        onValueChange = { filterNama = it },
                        placeholder = { Text("Cari kamar atau nama penghuni...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (uiState.tetapkanLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val filtered = uiState.tetapkanPenghuniList.filter {
                            filterNama.isBlank() ||
                                it.nama.contains(filterNama, ignoreCase = true) ||
                                it.kamar.contains(filterNama, ignoreCase = true)
                        }
                        if (filtered.isEmpty()) {
                            Text("Tidak ada penghuni aktif pada tanggal tersebut.", style = MaterialTheme.typography.bodySmall)
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                                items(filtered, key = { it.id }) { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.confirmTetapkanPemilik(p.id) }
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(p.label.ifBlank { "Kamar ${p.kamar} · ${p.nama}" }, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    HorizontalDivider(color = ext.line)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissTetapkanPemilik) {
                    Text("Batal")
                }
            },
        )
    }
}

@Composable
private fun LaundryRowCard(
    item: LaundryDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTetapkanPemilik: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val cardBg = if (item.lewatKuota) ext.dangerContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
    val judul = when (item.jenis) {
        "penghuni" -> {
            val kamar = item.kamarNomor?.let { "Kamar $it · " } ?: ""
            "$kamar${item.penghuniNama ?: "Penghuni"}"
        }
        "gedung" -> "GDG · Laundry Gedung"
        else -> "Tanpa Nama"
    }
    val biayaLabel = if (item.biayaUtama != null) {
        Formatters.formatRupiah(item.biayaUtama) + if (item.biayaManual) "*" else ""
    } else {
        if (item.beratCenti != null) "Belum diisi" else "-"
    }

    // Mobile-first: ketuk langsung pada kartu untuk melihat/mengubah catatan (tanpa ikon pensil)
    LaundryCard(
        modifier = Modifier.clickable(enabled = item.bolehUbah, onClick = onEdit),
        containerColor = cardBg,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Kiri: Judul + metadata ringkas (tanggal · berat · pencatat)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = judul,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (item.lewatKuota) ext.danger else MaterialTheme.colorScheme.onSurface,
                        )
                        if (item.lewatKuota) {
                            StatusBadge(
                                text = "Lewat",
                                containerColor = ext.dangerContainer,
                                contentColor = ext.danger,
                            )
                        } else if (item.jenis != "penghuni") {
                            StatusBadge(
                                text = item.jenisLabel.ifBlank { item.jenis },
                                containerColor = ext.goldContainer,
                                contentColor = ext.onGold,
                            )
                        }
                    }

                    val beratText = Formatters.formatBeratKg(item.beratCenti, item.beratLabel)
                    Text(
                        text = buildString {
                            append(DateUtil.formatIsoDateLabel(item.tanggal))
                            if (beratText != "-") append(" · $beratText")
                            item.dicatatOlehNama?.let { append(" · $it") }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                }

                // Kanan: Harga sejajar dengan ikon hapus ringkas (tanpa baris aksi bawah terpisah)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = biayaLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (item.biayaUtama == null && item.beratCenti != null) ext.warn else MaterialTheme.colorScheme.primary,
                        )
                        if (item.itemBiaya > 0) {
                            Text(
                                text = "+${Formatters.formatRupiah(item.itemBiaya)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ext.onGold,
                            )
                        }
                    }

                    if (item.bolehHapus) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Hapus",
                                tint = ext.danger.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            // Strip ringkas item terpisah jika ada
            if (item.item.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ext.goldContainer.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val ringkasItem = item.item.joinToString(", ") { sub ->
                        val qty = if (sub.jumlah > 1) " x${sub.jumlah}" else ""
                        "${sub.nama}$qty (${Formatters.formatRupiah(sub.biaya)})"
                    }
                    Text(
                        text = "Terpisah: $ringkasItem",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.onGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    )
                }
            }

            if (!item.catatan.isNullOrBlank()) {
                Text(
                    text = "Catatan: ${item.catatan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.muted,
                )
            }

            if (item.bolehTetapkanPemilik) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onTetapkanPemilik,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Icon(Icons.Default.PersonAddAlt1, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tetapkan Pemilik", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

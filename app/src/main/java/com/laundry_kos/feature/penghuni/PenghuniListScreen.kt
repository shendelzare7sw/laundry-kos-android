package com.laundry_kos.feature.penghuni

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.KuotaProgressBar
import com.laundry_kos.core.designsystem.KuotaVisualStatus
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.MonthSelectorBar
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.data.dto.KamarDto
import com.laundry_kos.data.dto.MasukPenghuniRequestDto
import com.laundry_kos.data.dto.PenghuniDto

val JANGKA_WAKTU_OPTIONS = listOf(
    "tidak_tentu" to "Tidak Tentu",
    "bulanan" to "Bulanan",
    "3_bulan" to "3 Bulan",
    "6_bulan" to "6 Bulan",
    "1_tahun" to "1 Tahun",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenghuniListScreen(
    viewModel: PenghuniListViewModel,
    onOpenDetail: (Int) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val bolehTambah = uiState.data?.bolehKelolaKontrak == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (bolehTambah) {
                FloatingActionButton(
                    onClick = viewModel::openTambahDialog,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Daftarkan Penghuni")
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
                    MonthSelectorBar(
                        bulanYm = uiState.bulan,
                        onChangeBulan = viewModel::setBulan,
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.cari,
                        onValueChange = viewModel::setCari,
                        placeholder = { Text("Cari nama, kamar, atau nomor WA...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                val hitungan = uiState.data?.hitungan
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf(
                            "semua" to "Semua (${hitungan?.semua ?: 0})",
                            "aktif" to "Aktif (${hitungan?.aktif ?: 0})",
                            "booking" to "Booking (${hitungan?.booking ?: 0})",
                            "keluar" to "Keluar (${hitungan?.keluar ?: 0})",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.tab == key,
                                onClick = { viewModel.setTab(key) },
                                label = { Text(label) },
                            )
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        ErrorBannerCard(message = uiState.error!!, onRetry = { viewModel.load() })
                    }
                }

                val data = uiState.data
                if (uiState.loading && data == null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (data != null && data.data.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Tidak ada data penghuni",
                            subtitle = "Ubah filter tab atau pencarian.",
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
                                pair.forEach { penghuni ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        PenghuniItemCard(
                                            penghuni = penghuni,
                                            onClick = { onOpenDetail(penghuni.id) },
                                        )
                                    }
                                }
                                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        items(data.data, key = { it.id }) { penghuni ->
                            PenghuniItemCard(
                                penghuni = penghuni,
                                onClick = { onOpenDetail(penghuni.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showTambahDialog) {
        TambahPenghuniDialog(
            kamarKosong = uiState.kamarKosong,
            loadingKamar = uiState.loadingKamar,
            submitting = uiState.submittingTambah,
            errorMessage = uiState.tambahError,
            onDismiss = viewModel::dismissTambahDialog,
            onSubmit = viewModel::submitTambahPenghuni,
        )
    }
}

@Composable
private fun PenghuniItemCard(
    penghuni: PenghuniDto,
    onClick: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val (statusBg, statusFg) = when (penghuni.status) {
        "aktif" -> ext.okContainer to ext.ok
        "booking" -> ext.infoContainer to ext.info
        else -> ext.sunken to ext.muted
    }

    LaundryCard(
        modifier = Modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val kamarPrefix = penghuni.kamarNomor?.let { "Kamar $it · " } ?: ""
                    Text(
                        text = "$kamarPrefix${penghuni.nama}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    val sub = buildList {
                        penghuni.noWa?.takeIf { it.isNotBlank() }?.let { add(it) }
                        add(penghuni.jangkaWaktuLabel)
                    }.joinToString(" · ")
                    Text(text = sub, style = MaterialTheme.typography.bodySmall, color = ext.muted)
                }
                StatusBadge(
                    text = penghuni.statusLabel.ifBlank { penghuni.status },
                    containerColor = statusBg,
                    contentColor = statusFg,
                )
            }

            penghuni.kuota?.let { kuota ->
                if (kuota.modeKuota != "tanpa_kuota") {
                    val visual = KuotaVisualStatus.fromApi(kuota.status)
                    KuotaProgressBar(persen = kuota.persenBar, status = visual)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${kuota.terpakaiKg} / ${kuota.kuotaKg} kg (${kuota.persen}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.muted,
                        )
                        if (kuota.totalTagihan > 0) {
                            Text(
                                text = "Tagihan: ${Formatters.formatRupiah(kuota.totalTagihan)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.danger,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TambahPenghuniDialog(
    kamarKosong: List<KamarDto>,
    loadingKamar: Boolean,
    submitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (MasukPenghuniRequestDto) -> Unit,
) {
    var nama by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var noWa by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("aktif") }
    var selectedKamar by remember { mutableStateOf<KamarDto?>(null) }
    var kamarExpanded by remember { mutableStateOf(false) }
    var tanggalMasuk by remember { mutableStateOf(DateUtil.todayIso()) }
    var perkiraan by remember { mutableStateOf(false) }
    var jangkaWaktu by remember { mutableStateOf("tidak_tentu") }
    var jangkaExpanded by remember { mutableStateOf(false) }
    var tanggalKeluarRencana by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text("Daftarkan Penghuni Baru") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    ErrorBannerCard(message = errorMessage)
                }

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Lengkap *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text("Panggilan / Alias") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = status == "aktif", onClick = { status = "aktif" }, label = { Text("Aktif") })
                    FilterChip(selected = status == "booking", onClick = { status = "booking" }, label = { Text("Booking") })
                }

                // Kamar Kosong Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedKamar?.let { "Kamar ${it.nomor} (Lt. ${it.lantai})" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kamar Kosong *") },
                        placeholder = { Text(if (loadingKamar) "Memuat kamar..." else "Pilih kamar kosong") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { kamarExpanded = true })
                    DropdownMenu(expanded = kamarExpanded, onDismissRequest = { kamarExpanded = false }) {
                        if (kamarKosong.isEmpty()) {
                            DropdownMenuItem(text = { Text("Tidak ada kamar kosong") }, onClick = { kamarExpanded = false })
                        } else {
                            kamarKosong.forEach { kamar ->
                                DropdownMenuItem(
                                    text = { Text("Kamar ${kamar.nomor} (Lt. ${kamar.lantai})") },
                                    onClick = {
                                        selectedKamar = kamar
                                        kamarExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                DatePickerField(
                    label = "Tanggal Masuk *",
                    valueIso = tanggalMasuk,
                    onValueSelected = { tanggalMasuk = it },
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { perkiraan = !perkiraan },
                ) {
                    Checkbox(checked = perkiraan, onCheckedChange = { perkiraan = it })
                    Text("Tanggal masuk perkiraan", style = MaterialTheme.typography.bodySmall)
                }

                // Jangka Waktu Dropdown
                Box {
                    val labelJangka = JANGKA_WAKTU_OPTIONS.firstOrNull { it.first == jangkaWaktu }?.second ?: "Tidak Tentu"
                    OutlinedTextField(
                        value = labelJangka,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jangka Waktu Sewa") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { jangkaExpanded = true })
                    DropdownMenu(expanded = jangkaExpanded, onDismissRequest = { jangkaExpanded = false }) {
                        JANGKA_WAKTU_OPTIONS.forEach { (key, lbl) ->
                            DropdownMenuItem(
                                text = { Text(lbl) },
                                onClick = {
                                    jangkaWaktu = key
                                    jangkaExpanded = false
                                },
                            )
                        }
                    }
                }

                DatePickerField(
                    label = "Rencana Keluar (opsional)",
                    valueIso = tanggalKeluarRencana,
                    onValueSelected = { tanggalKeluarRencana = it },
                )

                OutlinedTextField(
                    value = noWa,
                    onValueChange = { noWa = it },
                    label = { Text("Nomor WhatsApp") },
                    placeholder = { Text("08xxx / 628xxx") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !submitting && nama.isNotBlank() && selectedKamar != null,
                onClick = {
                    val kamarId = selectedKamar?.id ?: return@TextButton
                    onSubmit(
                        MasukPenghuniRequestDto(
                            nama = nama.trim(),
                            alias = alias.trim().takeIf { it.isNotEmpty() },
                            noWa = noWa.trim().takeIf { it.isNotEmpty() },
                            status = status,
                            kamarId = kamarId,
                            tanggalMasuk = tanggalMasuk,
                            tanggalMasukPerkiraan = perkiraan,
                            jangkaWaktuSewa = jangkaWaktu,
                            tanggalKeluarRencana = tanggalKeluarRencana.takeIf { it.isNotBlank() },
                            catatan = catatan.trim().takeIf { it.isNotEmpty() },
                        ),
                    )
                },
            ) {
                Text(if (submitting) "Menyimpan..." else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) {
                Text("Batal")
            }
        },
    )
}

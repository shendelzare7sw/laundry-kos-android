package com.laundry_kos.feature.laundry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.KuotaVisualStatus
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.MaroonPrimaryButton
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryFormScreen(
    viewModel: LaundryFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onOpenExistingEdit: (Int) -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    var showPenghuniPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.savedMessage) {
        if (uiState.savedMessage != null) {
            onSaved()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.editId != null) "Ubah Catatan Laundry" else "Catat Laundry",
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
        if (uiState.loadingInitial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 720.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.submitError?.let { err ->
                ErrorBannerCard(message = err.summaryMessage())
            }

            // Jenis & Tanggal
            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Jenis & Tanggal", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "penghuni" to "Penghuni",
                            "gedung" to "Gedung (GDG)",
                            "tanpa_nama" to "Tanpa Nama",
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = uiState.jenis == key,
                                onClick = { viewModel.setJenis(key) },
                                label = { Text(label) },
                            )
                        }
                    }

                    DatePickerField(
                        label = "Tanggal Laundry",
                        valueIso = uiState.tanggal,
                        onValueSelected = viewModel::setTanggal,
                        isError = uiState.submitError?.firstFieldError("tanggal") != null,
                        supportingText = uiState.submitError?.firstFieldError("tanggal"),
                    )

                    if (uiState.jenis == "penghuni") {
                        // 1 Combobox Penghuni yang dapat dicari
                        Box {
                            OutlinedTextField(
                                value = uiState.selectedPenghuniLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Penghuni (1 Combobox)") },
                                placeholder = { Text("Pilih kamar / penghuni...") },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                },
                                isError = uiState.submitError?.firstFieldError("penghuni_id") != null,
                                supportingText = uiState.submitError?.firstFieldError("penghuni_id")?.let { { Text(it) } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showPenghuniPicker = true },
                            )
                        }
                    }
                }
            }

            // Live Info Kuota & Deteksi Duplikat 1-tanggal-1-catatan
            val preview = uiState.preview
            if (uiState.jenis == "penghuni" && preview != null) {
                if (!preview.galat.isNullOrBlank()) {
                    LaundryCard(containerColor = ext.dangerContainer) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = ext.danger)
                                Text(
                                    text = preview.galat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.maroonDarker,
                                )
                            }
                            preview.duplikat?.let { dup ->
                                OutlinedButton(
                                    onClick = { onOpenExistingEdit(dup.id) },
                                    border = BorderStroke(1.dp, ext.danger),
                                ) {
                                    Text("Buka & Ubah Catatan yang Sudah Ada", color = ext.danger)
                                }
                            }
                        }
                    }
                }

                preview.kuota?.let { k ->
                    val statusVisual = KuotaVisualStatus.fromApi(k.status)
                    val badgeBg = when (statusVisual) {
                        KuotaVisualStatus.LEWAT -> ext.dangerContainer
                        KuotaVisualStatus.MENDEKATI -> ext.warnContainer
                        KuotaVisualStatus.NORMAL -> ext.okContainer
                    }
                    val badgeFg = when (statusVisual) {
                        KuotaVisualStatus.LEWAT -> ext.danger
                        KuotaVisualStatus.MENDEKATI -> ext.onGold
                        KuotaVisualStatus.NORMAL -> ext.ok
                    }
                    LaundryCard(containerColor = ext.surfaceAlt) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Pratinjau Kuota ${k.labelBulan}",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                if (k.modeKuota != "tanpa_kuota") {
                                    StatusBadge(
                                        text = when (statusVisual) {
                                            KuotaVisualStatus.LEWAT -> "Lewat ${k.lewatKg} kg"
                                            KuotaVisualStatus.MENDEKATI -> "Mendekati"
                                            KuotaVisualStatus.NORMAL -> "Normal"
                                        },
                                        containerColor = badgeBg,
                                        contentColor = badgeFg,
                                    )
                                }
                            }
                            if (k.modeKuota == "tanpa_kuota") {
                                Text("Properti ini tidak menerapkan kuota bulanan.", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                            } else {
                                Text(
                                    text = "Terpakai: ${k.sebelumKg} kg → ${k.sesudahKg} kg dari kuota ${k.kuotaKg} kg (Sisa: ${k.sisaKg} kg)",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            // Berat & Biaya Utama
            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Cucian Utama", style = MaterialTheme.typography.titleSmall)
                        if (preview != null) {
                            Text(
                                text = "Tarif ${Formatters.formatRupiah(preview.tarifPerKg)}/kg · Min ${preview.minKg} kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.muted,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.beratKg,
                        onValueChange = viewModel::setBeratKg,
                        label = { Text("Berat (kg)") },
                        placeholder = { Text("Contoh: 2.5 atau 2,5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.submitError?.firstFieldError("berat_kg") != null,
                        supportingText = uiState.submitError?.firstFieldError("berat_kg")?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (uiState.isBeratBesar || uiState.submitError?.firstFieldError("konfirmasi_berat_besar") != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setKonfirmasiBeratBesar(!uiState.konfirmasiBeratBesar) },
                        ) {
                            Checkbox(
                                checked = uiState.konfirmasiBeratBesar,
                                onCheckedChange = viewModel::setKonfirmasiBeratBesar,
                            )
                            Text(
                                text = "Saya yakin berat cucian melebihi 30 kg ini benar",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.danger,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ubah Biaya Utama Manual", style = MaterialTheme.typography.bodyMedium)
                            val autoLabel = preview?.biayaOtomatis?.let { Formatters.formatRupiah(it) } ?: "-"
                            Text(
                                text = "Hitungan server otomatis: $autoLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.muted,
                            )
                        }
                        Switch(
                            checked = uiState.biayaManualMode,
                            onCheckedChange = viewModel::setBiayaManualMode,
                        )
                    }

                    if (uiState.biayaManualMode) {
                        OutlinedTextField(
                            value = uiState.biayaUtamaInput,
                            onValueChange = viewModel::setBiayaUtamaInput,
                            label = { Text("Biaya Utama Manual (Rp)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.submitError?.firstFieldError("biaya_utama") != null,
                            supportingText = uiState.submitError?.firstFieldError("biaya_utama")?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            // Multi-Item Terpisah
            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Item Bayar Terpisah", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Selimut, bedcover, sepatu, jaket, dll. (tidak masuk kuota)",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.muted,
                            )
                        }
                        TextButton(onClick = viewModel::addItem) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah")
                        }
                    }

                    uiState.items.forEachIndexed { idx, draft ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ext.surfaceAlt,
                            border = BorderStroke(1.dp, ext.line),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Item #${idx + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ext.onGold,
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeItem(draft.uid) },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Hapus item", tint = ext.danger)
                                    }
                                }
                                OutlinedTextField(
                                    value = draft.nama,
                                    onValueChange = { v -> viewModel.updateItem(draft.uid) { it.copy(nama = v) } },
                                    label = { Text("Nama Item") },
                                    placeholder = { Text("Contoh: Bedcover King") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    OutlinedTextField(
                                        value = draft.jumlah,
                                        onValueChange = { v ->
                                            viewModel.updateItem(draft.uid) { it.copy(jumlah = v.filter(Char::isDigit)) }
                                        },
                                        label = { Text("Qty") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(0.7f),
                                    )
                                    OutlinedTextField(
                                        value = draft.beratKg,
                                        onValueChange = { v ->
                                            viewModel.updateItem(draft.uid) {
                                                it.copy(beratKg = v.filter { c -> c.isDigit() || c == '.' || c == ',' })
                                            }
                                        },
                                        label = { Text("Kg (ops)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(0.9f),
                                    )
                                    OutlinedTextField(
                                        value = draft.biaya,
                                        onValueChange = { v ->
                                            viewModel.updateItem(draft.uid) { it.copy(biaya = v.filter(Char::isDigit)) }
                                        },
                                        label = { Text("Biaya (Rp)") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1.4f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Catatan Opsional
            LaundryCard {
                OutlinedTextField(
                    value = uiState.catatan,
                    onValueChange = viewModel::setCatatan,
                    label = { Text("Catatan (opsional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            MaroonPrimaryButton(
                text = if (uiState.editId != null) "Simpan Perubahan" else "Simpan Catatan Laundry",
                loading = uiState.submitting,
                enabled = !uiState.hasDuplikatBlock,
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        }
    }

    // Searchable Penghuni Combobox Dialog
    if (showPenghuniPicker) {
        var search by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPenghuniPicker = false },
            title = { Text("Pilih Penghuni (${uiState.tanggal})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Ketik nomor kamar atau nama...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (uiState.loadingPreview && uiState.daftarPenghuni.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val filtered = uiState.daftarPenghuni.filter {
                            search.isBlank() ||
                                it.nama.contains(search, ignoreCase = true) ||
                                (it.alias?.contains(search, ignoreCase = true) == true) ||
                                it.kamar.contains(search, ignoreCase = true)
                        }
                        if (filtered.isEmpty()) {
                            Text(
                                "Tidak ada penghuni aktif yang menempati kamar pada tanggal ini.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.muted,
                            )
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                                items(filtered, key = { it.id }) { p ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.selectPenghuni(p)
                                                showPenghuniPicker = false
                                            }
                                            .padding(vertical = 10.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = p.label.ifBlank { "Kamar ${p.kamar} · ${p.nama}" },
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    HorizontalDivider(color = ext.line)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPenghuniPicker = false }) {
                    Text("Tutup")
                }
            },
        )
    }
}

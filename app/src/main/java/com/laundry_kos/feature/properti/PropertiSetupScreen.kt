package com.laundry_kos.feature.properti

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.MaroonPrimaryButton
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.RupiahVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiSetupScreen(
    viewModel: PropertiSetupViewModel,
    onBack: () -> Unit,
    onOpenKelolaPengguna: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val ext = LaundryKosTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }

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
                    Column {
                        Text(
                            text = "Setup Gedung",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        uiState.data?.properti?.nama?.let { nama ->
                            Text(nama, color = ext.goldSoft, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            )
        },
    ) { padding ->
        if (uiState.loading && uiState.data == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.error != null) {
                    ErrorBannerCard(message = uiState.error!!, onRetry = viewModel::load)
                }

                val data = uiState.data
                if (data != null) {
                    // 1. Checklist Kesiapan & Aktivasi Properti
                    LaundryCard(containerColor = ext.surfaceAlt) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Status Kesiapan Properti", style = MaterialTheme.typography.titleSmall)
                                StatusBadge(
                                    text = data.properti.status.replaceFirstChar { it.uppercase() },
                                    containerColor = if (data.properti.status == "aktif") ext.okContainer else ext.goldContainer,
                                    contentColor = if (data.properti.status == "aktif") ext.ok else ext.onGold,
                                )
                            }

                            ChecklistRow("Minimal 1 kamar aktif", data.siapKamar)
                            ChecklistRow("Aturan laundry & kuota tersimpan", data.siapAturan)
                            ChecklistRow("Admin properti aktif ditetapkan", data.siapAdmin)

                            if (data.properti.status != "aktif") {
                                Button(
                                    onClick = viewModel::aktifkanProperti,
                                    enabled = !uiState.saving,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ext.ok,
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Text("Aktifkan Properti Sekarang", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Kamar per Lantai + Generate Kamar
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Generate Kamar per Lantai", style = MaterialTheme.typography.titleSmall)
                            if (data.kamarPerLantai.isNotEmpty()) {
                                data.kamarPerLantai.forEach { group ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text("Lantai ${group.lantai}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "${group.jumlah} kamar (${group.nomor.firstOrNull() ?: "-"} – ${group.nomor.lastOrNull() ?: "-"})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.muted,
                                        )
                                    }
                                    HorizontalDivider(color = ext.line)
                                }
                            } else {
                                Text("Belum ada kamar terdaftar.", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.lantaiInput,
                                    onValueChange = { v -> viewModel.update { it.copy(lantaiInput = v.filter(Char::isDigit)) } },
                                    label = { Text("Lantai") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.8f),
                                )
                                OutlinedTextField(
                                    value = uiState.dariInput,
                                    onValueChange = { v -> viewModel.update { it.copy(dariInput = v.filter(Char::isDigit)) } },
                                    label = { Text("Dari No") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.1f),
                                )
                                OutlinedTextField(
                                    value = uiState.sampaiInput,
                                    onValueChange = { v -> viewModel.update { it.copy(sampaiInput = v.filter(Char::isDigit)) } },
                                    label = { Text("Sampai No") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1.1f),
                                )
                            }

                            MaroonPrimaryButton(
                                text = "Tambah Rentang Kamar",
                                loading = uiState.saving,
                                onClick = viewModel::generateKamar,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // 3. Aturan Laundry Awal
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Aturan Laundry Properti", style = MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.tarifPerKg,
                                    onValueChange = { v -> viewModel.update { it.copy(tarifPerKg = v.filter(Char::isDigit)) } },
                                    label = { Text("Tarif/kg (Rp)") },
                                    singleLine = true,
                                    visualTransformation = RupiahVisualTransformation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = uiState.minKg,
                                    onValueChange = { v -> viewModel.update { it.copy(minKg = v) } },
                                    label = { Text("Min Kg") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.kuotaKg,
                                    onValueChange = { v -> viewModel.update { it.copy(kuotaKg = v) } },
                                    label = { Text("Kuota (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = uiState.toleransiKg,
                                    onValueChange = { v -> viewModel.update { it.copy(toleransiKg = v) } },
                                    label = { Text("Toleransi (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.ambangPersen,
                                    onValueChange = { v -> viewModel.update { it.copy(ambangPersen = v.filter(Char::isDigit)) } },
                                    label = { Text("Ambang (%)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = uiState.jamEditPenjaga,
                                    onValueChange = { v -> viewModel.update { it.copy(jamEditPenjaga = v.filter(Char::isDigit)) } },
                                    label = { Text("Edit Penjaga (jam)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                listOf(1, 100, 500, 1000, 5000).forEach { nominal ->
                                    FilterChip(
                                        selected = uiState.pembulatan == nominal,
                                        onClick = { viewModel.update { it.copy(pembulatan = nominal) } },
                                        label = { Text("Bulat ${Formatters.formatRupiah(nominal)}") },
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = uiState.modeKuota == "penghuni",
                                    onClick = { viewModel.update { it.copy(modeKuota = "penghuni") } },
                                    label = { Text("Per Penghuni") },
                                )
                                FilterChip(
                                    selected = uiState.modeKuota == "tanpa_kuota",
                                    onClick = { viewModel.update { it.copy(modeKuota = "tanpa_kuota") } },
                                    label = { Text("Tanpa Kuota") },
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Penjaga boleh kelola kontrak", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = uiState.penjagaKelolaKontrak,
                                    onCheckedChange = { c -> viewModel.update { it.copy(penjagaKelolaKontrak = c) } },
                                )
                            }
                            MaroonPrimaryButton(
                                text = "Simpan Aturan Setup",
                                loading = uiState.saving,
                                onClick = viewModel::simpanAturan,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // 4. Staf Properti (Admin & Penjaga)
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Admin & Penjaga Properti", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "Admin aktif: ${data.adminAktif?.let { "${it.name} (@${it.username})" } ?: "Belum ditetapkan"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (data.adminAktif != null) ext.ok else ext.danger,
                            )
                            if (data.penjaga.isNotEmpty()) {
                                Text(
                                    text = "Penjaga: " + data.penjaga.joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.muted,
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenKelolaPengguna,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Kelola / Tambah Akun Staf")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(label: String, done: Boolean) {
    val ext = LaundryKosTheme.extendedColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) ext.ok else ext.muted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (done) MaterialTheme.colorScheme.onSurface else ext.muted,
        )
    }
}

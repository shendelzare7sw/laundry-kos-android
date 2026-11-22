package com.laundry_kos.feature.pengaturan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.DatePickerField
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.MaroonPrimaryButton
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.core.util.RupiahVisualTransformation
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.data.dto.UserDto

private val PEMBULATAN_OPTIONS = listOf(1, 100, 500, 1000, 5000)

@Composable
fun PengaturanScreen(
    viewModel: PengaturanViewModel?,
    user: UserDto,
    propertiAktif: PropertiDto,
    bolehKelolaPengaturan: Boolean,
    canSwitchProperti: Boolean,
    onSwitchProperti: () -> Unit,
    onLogout: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState = viewModel?.state?.collectAsStateWithLifecycle()?.value

    LaunchedEffect(uiState?.snackbarMessage) {
        uiState?.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel?.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
    ) { padding ->
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
                // Kartu Akun & Sesi (Selalu tampil untuk semua role)
                LaundryCard(containerColor = ext.surfaceAlt) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Akun & Sesi",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "@${user.username} · ${user.roleLabel.ifBlank { user.role }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.muted,
                                )
                            }
                            StatusBadge(
                                text = propertiAktif.nama,
                                containerColor = ext.goldContainer,
                                contentColor = ext.onGold,
                            )
                        }
                        if (!propertiAktif.alamat.isNullOrBlank()) {
                            Text(
                                text = propertiAktif.alamat,
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.muted,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (canSwitchProperti) {
                                OutlinedButton(
                                    onClick = onSwitchProperti,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                ) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ganti Properti")
                                }
                            }
                            OutlinedButton(
                                onClick = onLogout,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ext.danger),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.danger),
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Keluar")
                            }
                        }
                    }
                }

                // Pengaturan Properti HANYA tampil untuk role yang berwenang (admin / owner / dev_admin).
                // Role penjaga tidak ditampilkan form aturan, izin penjaga, maupun template WA.
                if (!bolehKelolaPengaturan || viewModel == null || uiState == null) {
                    return@Column
                }

                if (uiState.error != null) {
                    ErrorBannerCard(message = uiState.error, onRetry = { viewModel.load() })
                }

                if (uiState.loading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.bolehUbah) {
                    // 1. Aturan Tarif & Kuota Berversi
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Aturan Tarif & Kuota Berversi", style = MaterialTheme.typography.titleSmall)

                            DatePickerField(
                                label = "Lihat Aturan Aktif Pada Tanggal",
                                valueIso = uiState.tanggalAcuan,
                                onValueSelected = { viewModel.load(it) },
                            )

                            OutlinedTextField(
                                value = uiState.namaProperti,
                                onValueChange = { v -> viewModel.updateState { it.copy(namaProperti = v) } },
                                label = { Text("Nama Properti pada Laporan") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.tarifPerKg,
                                    onValueChange = { v -> viewModel.updateState { it.copy(tarifPerKg = v.filter(Char::isDigit)) } },
                                    label = { Text("Tarif / kg (Rp)") },
                                    singleLine = true,
                                    visualTransformation = RupiahVisualTransformation,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = uiState.minKg,
                                    onValueChange = { v -> viewModel.updateState { it.copy(minKg = v) } },
                                    label = { Text("Min Berat (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.kuotaKg,
                                    onValueChange = { v -> viewModel.updateState { it.copy(kuotaKg = v) } },
                                    label = { Text("Kuota Gratis (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                                OutlinedTextField(
                                    value = uiState.toleransiKg,
                                    onValueChange = { v -> viewModel.updateState { it.copy(toleransiKg = v) } },
                                    label = { Text("Toleransi (kg)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            OutlinedTextField(
                                value = uiState.ambangPersen,
                                onValueChange = { v -> viewModel.updateState { it.copy(ambangPersen = v.filter(Char::isDigit)) } },
                                label = { Text("Ambang Mendekati Kuota (%)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Text("Mode Kuota", style = MaterialTheme.typography.labelMedium, color = ext.muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = uiState.modeKuota == "penghuni",
                                    onClick = { viewModel.updateState { it.copy(modeKuota = "penghuni") } },
                                    label = { Text("Per Penghuni") },
                                )
                                FilterChip(
                                    selected = uiState.modeKuota == "tanpa_kuota",
                                    onClick = { viewModel.updateState { it.copy(modeKuota = "tanpa_kuota") } },
                                    label = { Text("Tanpa Kuota") },
                                )
                            }

                            Text("Pembulatan Biaya & Tagihan (Rp)", style = MaterialTheme.typography.labelMedium, color = ext.muted)
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                PEMBULATAN_OPTIONS.forEach { nominal ->
                                    FilterChip(
                                        selected = uiState.pembulatan == nominal,
                                        onClick = { viewModel.updateState { it.copy(pembulatan = nominal) } },
                                        label = { Text(Formatters.formatRupiah(nominal)) },
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Kuota prorata bulan masuk", style = MaterialTheme.typography.bodyMedium)
                                Switch(
                                    checked = uiState.kuotaProrata,
                                    onCheckedChange = { checked -> viewModel.updateState { it.copy(kuotaProrata = checked) } },
                                )
                            }

                            DatePickerField(
                                label = "Berlaku Mulai Tanggal",
                                valueIso = uiState.berlakuMulai,
                                onValueSelected = { tgl -> viewModel.updateState { it.copy(berlakuMulai = tgl) } },
                            )
                            MaroonPrimaryButton(
                                text = "Simpan Aturan Tarif & Kuota",
                                loading = uiState.saving,
                                onClick = viewModel::simpanAturan,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // 2. Izin Penjaga
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Izin & Batas Edit Penjaga", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = uiState.jamEditPenjaga,
                                onValueChange = { v -> viewModel.updateState { it.copy(jamEditPenjaga = v.filter(Char::isDigit)) } },
                                label = { Text("Batas Jam Edit Penjaga (kosongkan = tanpa batas)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Penjaga boleh kelola kontrak penghuni", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = uiState.penjagaKelolaKontrak,
                                    onCheckedChange = { c -> viewModel.updateState { it.copy(penjagaKelolaKontrak = c) } },
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Penjaga boleh ubah catatan staf lain", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = uiState.penjagaUbahCatatanLain,
                                    onCheckedChange = { c -> viewModel.updateState { it.copy(penjagaUbahCatatanLain = c) } },
                                )
                            }
                            MaroonPrimaryButton(
                                text = "Simpan Izin Penjaga",
                                loading = uiState.saving,
                                onClick = viewModel::simpanIzinPenjaga,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // 3. Template Pesan WhatsApp
                    LaundryCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Template Pesan WhatsApp", style = MaterialTheme.typography.titleSmall)
                                TextButton(onClick = { viewModel.resetTemplateWa(null) }) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset Semua")
                                }
                            }

                            OutlinedTextField(
                                value = uiState.templateWaTagihan,
                                onValueChange = { v -> viewModel.updateState { it.copy(templateWaTagihan = v) } },
                                label = { Text("Template WA Tagihan") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = uiState.templateWaPeringatan,
                                onValueChange = { v -> viewModel.updateState { it.copy(templateWaPeringatan = v) } },
                                label = { Text("Template WA Peringatan Kuota") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            OutlinedTextField(
                                value = uiState.templateWaRekap,
                                onValueChange = { v -> viewModel.updateState { it.copy(templateWaRekap = v) } },
                                label = { Text("Template WA Rekap Gedung") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            MaroonPrimaryButton(
                                text = "Simpan Template WA",
                                loading = uiState.saving,
                                onClick = viewModel::simpanTemplateWa,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.laundry_kos.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.data.dto.PropertiDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiPickerScreen(
    uiState: SessionUiState.SelectProperti,
    onSelect: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pilih Properti Aktif",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "${uiState.user.name} (${uiState.user.roleLabel.ifBlank { uiState.user.role }})",
                            color = ext.goldSoft,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Keluar",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!uiState.message.isNullOrBlank()) {
                ErrorBannerCard(message = uiState.message)
            }

            if (uiState.isSubmitting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(uiState.propertiList, key = { it.id }) { properti ->
                    PropertiItemCard(
                        properti = properti,
                        selected = false,
                        enabled = !uiState.isSubmitting && properti.status == "aktif",
                        onClick = { onSelect(properti.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun PropertiSwitcherDialog(
    propertiList: List<PropertiDto>,
    activeId: Int,
    isSwitching: Boolean,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSwitching) onDismiss() },
        title = { Text("Ganti Properti Aktif") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isSwitching) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                propertiList.forEach { properti ->
                    PropertiItemCard(
                        properti = properti,
                        selected = properti.id == activeId,
                        enabled = !isSwitching && properti.status == "aktif",
                        onClick = { onSelect(properti.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isSwitching) {
                Text("Tutup")
            }
        },
    )
}

@Composable
private fun PropertiItemCard(
    properti: PropertiDto,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    LaundryCard(
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        containerColor = if (selected) ext.goldContainer else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Apartment,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else ext.gold,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = properti.nama, style = MaterialTheme.typography.titleMedium)
                if (!properti.alamat.isNullOrBlank()) {
                    Text(
                        text = properti.alamat,
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                }
                val infoParts = buildList {
                    properti.jumlahKamar?.let { add("$it kamar") }
                    properti.jumlahPenghuniAktif?.let { add("$it penghuni aktif") }
                }
                if (infoParts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = infoParts.joinToString(" • "),
                        style = MaterialTheme.typography.labelMedium,
                        color = ext.onGold,
                    )
                }
            }
            if (properti.status != "aktif") {
                StatusBadge(
                    text = "Nonaktif",
                    containerColor = ext.dangerContainer,
                    contentColor = ext.danger,
                )
            }
        }
    }
}

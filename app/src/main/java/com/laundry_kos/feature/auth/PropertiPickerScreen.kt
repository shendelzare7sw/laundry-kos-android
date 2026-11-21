package com.laundry_kos.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.feature.notifikasi.AppNotification
import com.laundry_kos.feature.notifikasi.NotificationBellButton
import com.laundry_kos.feature.notifikasi.NotificationListDialog
import com.laundry_kos.feature.notifikasi.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiPickerScreen(
    uiState: SessionUiState.SelectProperti,
    onSelect: (Int) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    var showAkunDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // Notifikasi global persis AppLayout.php baris 76-97: properti yang masih draf / belum aktif
    val globalNotifications = remember(uiState.propertiList) {
        uiState.propertiList
            .filter { it.status != "aktif" }
            .map { prop ->
                AppNotification(
                    id = "prop_${prop.id}",
                    type = NotificationType.INFO,
                    title = "Lengkapi setup ${prop.nama}",
                    subtitle = "Status saat ini: ${prop.status}.",
                    penghuniId = null,
                )
            }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(ext.gold),
                        )
                        Column {
                            Text(
                                text = "Gedung",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Pilih properti untuk dikelola",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.muted,
                            )
                        }
                    }
                },
                actions = {
                    NotificationBellButton(
                        count = globalNotifications.size,
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { showNotifDialog = true },
                    )
                    IconButton(onClick = { showAkunDialog = true }) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(32.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Profil Akun",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Keluar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!uiState.message.isNullOrBlank()) {
                    item {
                        ErrorBannerCard(message = uiState.message)
                    }
                }

                if (uiState.isSubmitting) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                if (uiState.propertiList.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum ada gedung terdaftar",
                            subtitle = "Tambahkan properti melalui panel web atau hubungi administrator.",
                        )
                    }
                } else if (isWide) {
                    items(uiState.propertiList.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            pair.forEach { properti ->
                                GedungItemCard(
                                    properti = properti,
                                    enabled = !uiState.isSubmitting && properti.status == "aktif",
                                    onOpen = { onSelect(properti.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    items(uiState.propertiList, key = { it.id }) { properti ->
                        GedungItemCard(
                            properti = properti,
                            enabled = !uiState.isSubmitting && properti.status == "aktif",
                            onOpen = { onSelect(properti.id) },
                        )
                    }
                }
            }
        }
    }

    if (showNotifDialog) {
        NotificationListDialog(
            notifications = globalNotifications,
            emptyMessage = "Semua properti sudah aktif dan siap dikelola.",
            onItemClick = { showNotifDialog = false },
            onSeeAllPenghuni = null,
            onDismiss = { showNotifDialog = false },
        )
    }

    if (showAkunDialog) {
        AlertDialog(
            onDismissRequest = { showAkunDialog = false },
            title = { Text("Akun Aktif") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(uiState.user.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "@${uiState.user.username} · ${uiState.user.roleLabel.ifBlank { uiState.user.role }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                    Text(
                        text = "Total akses gedung: ${uiState.propertiList.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAkunDialog = false }) { Text("Tutup") }
            },
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Keluar dari Laundry-Kos?") },
            text = { Text("Sesi login Anda akan diakhiri dan Anda perlu masuk kembali untuk mengakses aplikasi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                ) {
                    Text("Ya, Keluar", color = ext.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Batal") }
            },
        )
    }
}

@Composable
private fun GedungItemCard(
    properti: PropertiDto,
    enabled: Boolean,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    val isAktif = properti.status == "aktif"
    val (statusBg, statusFg, statusText) = when (properti.status) {
        "aktif" -> Triple(ext.ok, Color.White, "Aktif")
        "draf" -> Triple(ext.goldContainer, ext.onGold, "Draf")
        else -> Triple(ext.sunken, ext.muted, properti.status.replaceFirstChar { it.uppercase() })
    }

    LaundryCard(
        modifier = modifier.clickable(enabled = enabled, onClick = onOpen),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.size(44.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Apartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = properti.nama,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(
                            text = statusText,
                            containerColor = statusBg,
                            contentColor = statusFg,
                        )
                    }

                    Text(
                        text = properti.alamat?.takeIf { it.isNotBlank() } ?: "Alamat belum dilengkapi",
                        style = MaterialTheme.typography.bodySmall,
                        color = ext.muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (properti.jumlahKamar != null || properti.jumlahPenghuniAktif != null) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp),
                        ) {
                            properti.jumlahKamar?.let { kamar ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Default.DoorFront, contentDescription = null, tint = ext.subtle, modifier = Modifier.size(14.dp))
                                    Text("$kamar kamar", style = MaterialTheme.typography.labelSmall, color = ext.muted)
                                }
                            }
                            properti.jumlahPenghuniAktif?.let { penghuni ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, tint = ext.subtle, modifier = Modifier.size(14.dp))
                                    Text("$penghuni penghuni aktif", style = MaterialTheme.typography.labelSmall, color = ext.muted)
                                }
                            }
                        }
                    }
                }
            }

            if (isAktif) {
                Button(
                    onClick = onOpen,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buka Properti", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ext.sunken,
                    border = BorderStroke(1.dp, ext.line),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Properti berstatus ${properti.status} — selesaikan aktivasi di panel web untuk membuka operasional.",
                        style = MaterialTheme.typography.labelSmall,
                        color = ext.muted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

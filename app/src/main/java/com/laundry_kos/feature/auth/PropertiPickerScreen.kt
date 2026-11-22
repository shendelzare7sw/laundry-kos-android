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
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.laundry_kos.core.designsystem.EmptyStateCard
import com.laundry_kos.core.designsystem.ErrorBannerCard
import com.laundry_kos.core.designsystem.LaundryCard
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.LaundrySnackbarHost
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.designsystem.rememberIsWideLayout
import com.laundry_kos.core.notifikasi.NotificationStore
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.data.dto.SimpanPropertiRequestDto
import com.laundry_kos.feature.notifikasi.AppNotification
import com.laundry_kos.feature.notifikasi.NotificationBellButton
import com.laundry_kos.feature.notifikasi.NotificationListDialog
import com.laundry_kos.feature.notifikasi.NotificationType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertiPickerScreen(
    uiState: SessionUiState.SelectProperti,
    notificationStore: NotificationStore,
    onSelect: (Int) -> Unit,
    onRefresh: () -> Unit,
    onTambahProperti: (SimpanPropertiRequestDto, onCreated: (PropertiDto) -> Unit) -> Unit,
    onUbahProperti: (Int, SimpanPropertiRequestDto, onUpdated: () -> Unit) -> Unit,
    onOpenSetup: (Int) -> Unit,
    onOpenPengguna: () -> Unit,
    onClearDialogError: () -> Unit,
    onClearSnackbar: () -> Unit,
    onLogout: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val isWide = rememberIsWideLayout()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notifStoreState by notificationStore.state.collectAsStateWithLifecycle()

    var showTambahDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<PropertiDto?>(null) }
    var showNotifDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSnackbar()
        }
    }

    // Notifikasi global persis AppLayout.php baris 76-97: properti draf / tanpa admin aktif
    val globalNotifications = remember(uiState.propertiList, notifStoreState.readIds, notifStoreState.dismissedIds) {
        uiState.propertiList
            .filter { it.status != "aktif" || it.adminAktifNama.isNullOrBlank() }
            .map { prop ->
                val id = "prop_${prop.id}"
                AppNotification(
                    id = id,
                    type = NotificationType.INFO,
                    title = "Lengkapi setup ${prop.nama}",
                    subtitle = if (prop.status != "aktif") "Status saat ini: ${prop.status}." else "Admin aktif belum ditentukan.",
                    penghuniId = prop.id, // digunakan untuk buka setup properti
                    isRead = id in notifStoreState.readIds,
                )
            }
            .filterNot { it.id in notifStoreState.dismissedIds }
    }
    val globalUnreadCount = globalNotifications.count { !it.isRead }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { LaundrySnackbarHost(snackbarHostState) },
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
                                text = "Pilih atau kelola properti",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.muted,
                            )
                        }
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onClearDialogError()
                            showTambahDialog = true
                        },
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    NotificationBellButton(
                        count = globalUnreadCount,
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { showNotifDialog = true },
                    )
                    IconButton(onClick = onOpenPengguna) {
                        Icon(
                            Icons.Default.ManageAccounts,
                            contentDescription = "Kelola Pengguna",
                            tint = MaterialTheme.colorScheme.primary,
                        )
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
                    item { ErrorBannerCard(message = uiState.message) }
                }

                if (uiState.isSubmitting) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                            subtitle = "Tekan tombol + Tambah di atas untuk membuat properti pertama.",
                        )
                    }
                } else if (isWide) {
                    val isGlobalRole = uiState.user.role == "owner" || uiState.user.role == "dev_admin"
                    items(uiState.propertiList.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            pair.forEach { properti ->
                                GedungItemCard(
                                    properti = properti,
                                    canManage = properti.bolehUbah || isGlobalRole,
                                    enabled = !uiState.isSubmitting,
                                    onOpen = { onSelect(properti.id) },
                                    onEdit = {
                                        onClearDialogError()
                                        editTarget = properti
                                    },
                                    onSetup = { onOpenSetup(properti.id) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    val isGlobalRole = uiState.user.role == "owner" || uiState.user.role == "dev_admin"
                    items(uiState.propertiList, key = { it.id }) { properti ->
                        GedungItemCard(
                            properti = properti,
                            canManage = properti.bolehUbah || isGlobalRole,
                            enabled = !uiState.isSubmitting,
                            onOpen = { onSelect(properti.id) },
                            onEdit = {
                                onClearDialogError()
                                editTarget = properti
                            },
                            onSetup = { onOpenSetup(properti.id) },
                        )
                    }
                }
            }
        }
    }

    // Modal Tambah Properti Baru (persis `properti/index.blade.php`)
    if (showTambahDialog) {
        PropertiFormDialog(
            title = "Tambah Properti Baru",
            initial = null,
            submitting = uiState.isSubmitting,
            errorMessage = uiState.dialogError,
            onDismiss = {
                onClearDialogError()
                showTambahDialog = false
            },
            onSubmit = { req ->
                onTambahProperti(req) { created ->
                    showTambahDialog = false
                    if (created.status == "draf") {
                        onOpenSetup(created.id)
                    }
                }
            },
        )
    }

    // Modal Edit Properti (persis `/properti/{id}/edit`)
    editTarget?.let { prop ->
        PropertiFormDialog(
            title = "Edit Properti",
            initial = prop,
            submitting = uiState.isSubmitting,
            errorMessage = uiState.dialogError,
            onDismiss = {
                onClearDialogError()
                editTarget = null
            },
            onSubmit = { req ->
                onUbahProperti(prop.id, req) {
                    editTarget = null
                }
            },
        )
    }

    if (showNotifDialog) {
        NotificationListDialog(
            notifications = globalNotifications,
            emptyMessage = "Semua properti sudah aktif dan memiliki admin.",
            onItemClick = { notif ->
                scope.launch { notificationStore.markAsRead(notif.id) }
                showNotifDialog = false
                notif.penghuniId?.let(onOpenSetup)
            },
            onMarkAllRead = {
                val ids = globalNotifications.map { it.id }
                scope.launch { notificationStore.markAllAsRead(ids) }
            },
            onClearAll = {
                val ids = globalNotifications.map { it.id }
                scope.launch { notificationStore.clearAll(propertiId = null, allVisibleIds = ids) }
            },
            onSeeAllPenghuni = null,
            onDismiss = { showNotifDialog = false },
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
                ) { Text("Ya, Keluar", color = ext.danger) }
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
    canManage: Boolean,
    enabled: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onSetup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    val isAktif = properti.status == "aktif"
    val (statusBg, statusFg, statusText) = when (properti.status) {
        "aktif" -> Triple(ext.okContainer, ext.ok, "Aktif")
        "draf" -> Triple(ext.goldContainer, ext.onGold, "Draf")
        else -> Triple(ext.sunken, ext.muted, properti.status.replaceFirstChar { it.uppercase() })
    }

    LaundryCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    modifier = Modifier.size(46.dp),
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

                    // 3 metadata persis Gambar 2 web: kamar, penghuni, admin aktif
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.DoorFront, contentDescription = null, tint = ext.subtle, modifier = Modifier.size(14.dp))
                            Text("${properti.jumlahKamar ?: 0} kamar", style = MaterialTheme.typography.labelSmall, color = ext.muted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = ext.subtle, modifier = Modifier.size(14.dp))
                            Text("${properti.jumlahPenghuniAktif ?: 0} penghuni", style = MaterialTheme.typography.labelSmall, color = ext.muted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = ext.subtle, modifier = Modifier.size(14.dp))
                            Text(
                                text = properti.adminAktifNama?.takeIf { it.isNotBlank() } ?: "Belum ada admin",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ext.line)

            // Baris Aksi Persis Gambar 2 Web: [→ Buka Properti] [✏️ Edit] [🎛️ Setup]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onOpen,
                    enabled = enabled && isAktif,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAktif) "Buka Properti" else "Belum Aktif (${properti.status})",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }

                if (canManage) {
                    OutlinedIconButton(
                        onClick = onEdit,
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ext.lineStrong),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Properti",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    OutlinedIconButton(
                        onClick = onSetup,
                        enabled = enabled,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ext.lineStrong),
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Setup Properti",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertiFormDialog(
    title: String,
    initial: PropertiDto?,
    submitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (SimpanPropertiRequestDto) -> Unit,
) {
    var nama by remember { mutableStateOf(initial?.nama ?: "") }
    var namaLaporan by remember { mutableStateOf(initial?.namaPropertiLaporan ?: initial?.nama ?: "") }
    var alamat by remember { mutableStateOf(initial?.alamat ?: "") }
    var noWaAdmin by remember { mutableStateOf(initial?.noWaAdmin ?: "") }
    var deskripsi by remember { mutableStateOf(initial?.deskripsi ?: "") }
    var status by remember { mutableStateOf(initial?.status ?: "draf") }

    AlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() },
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    ErrorBannerCard(message = errorMessage)
                }
                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        if (initial == null && namaLaporan.isBlank()) namaLaporan = it
                    },
                    label = { Text("Nama Properti *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = namaLaporan,
                    onValueChange = { namaLaporan = it },
                    label = { Text("Nama Properti di Laporan *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Alamat") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = noWaAdmin,
                    onValueChange = { noWaAdmin = it },
                    label = { Text("No WhatsApp Admin") },
                    placeholder = { Text("Contoh: 628xxxx") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Status Properti", style = MaterialTheme.typography.labelMedium, color = LaundryKosTheme.extendedColors.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("draf" to "Draf", "aktif" to "Aktif", "arsip" to "Arsip").forEach { (key, label) ->
                        FilterChip(
                            selected = status == key,
                            onClick = { status = key },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !submitting && nama.isNotBlank(),
                onClick = {
                    val namaFinal = nama.trim()
                    onSubmit(
                        SimpanPropertiRequestDto(
                            nama = namaFinal,
                            namaPropertiLaporan = namaLaporan.trim().ifBlank { namaFinal },
                            alamat = alamat.trim().takeIf { it.isNotEmpty() },
                            noWaAdmin = noWaAdmin.trim().takeIf { it.isNotEmpty() },
                            deskripsi = deskripsi.trim().takeIf { it.isNotEmpty() },
                            status = status,
                        ),
                    )
                },
            ) { Text(if (submitting) "Menyimpan..." else "Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !submitting) { Text("Batal") }
        },
    )
}

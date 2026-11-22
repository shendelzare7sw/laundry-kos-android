package com.laundry_kos.feature.notifikasi

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.laundry_kos.core.designsystem.LaundryKosTheme
import com.laundry_kos.core.designsystem.StatusBadge
import com.laundry_kos.core.network.ApiResult
import com.laundry_kos.core.notifikasi.ActivitySyncEngine
import com.laundry_kos.core.notifikasi.NotificationStore
import com.laundry_kos.core.notifikasi.StoredNotification
import com.laundry_kos.core.notifikasi.SystemNotificationHelper
import com.laundry_kos.core.util.DateUtil
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.data.repository.LaundryKosRepository
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class NotificationType {
    ACTIVITY,
    INFO,
    DANGER,
    WARN,
}

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val subtitle: String,
    val penghuniId: Int?,
    val laundryId: Int? = null,
    val timestampLabel: String? = null,
    val isRead: Boolean = false,
)

class NotifikasiViewModel(
    private val repository: LaundryKosRepository,
    private val appContext: Context,
    private val notificationStore: NotificationStore,
    private val activitySyncEngine: ActivitySyncEngine,
    private val propertiAktif: PropertiDto,
    private val currentUserId: Int,
    private val isGlobalRole: Boolean,
) : ViewModel() {

    private val _liveAlerts = MutableStateFlow<List<AppNotification>>(emptyList())
    private var isSyncing = false

    val notifications: StateFlow<List<AppNotification>> = combine(
        _liveAlerts,
        notificationStore.state,
    ) { liveAlerts, storeState ->
        val historyItems = storeState.history
            .asSequence()
            .filter { it.propertiId == propertiAktif.id && it.id !in storeState.dismissedIds }
            .map { stored ->
                stored.toAppNotification(isRead = stored.id in storeState.readIds)
            }
            .toList()

        val liveItems = liveAlerts
            .asSequence()
            .filter { it.id !in storeState.dismissedIds }
            .map { alert -> alert.copy(isRead = alert.id in storeState.readIds) }
            .toList()

        (historyItems + liveItems).distinctBy { it.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = emptyList(),
    )

    val unreadCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0,
        )

    init {
        refresh(emitSystem = true)
        viewModelScope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                refresh(emitSystem = true)
            }
        }
    }

    fun refresh(emitSystem: Boolean = true) {
        if (isSyncing) return
        viewModelScope.launch {
            isSyncing = true
            try {
                // 1. Deteksi perubahan data lintas role & kirim notifikasi sistem Android
                activitySyncEngine.syncActiveProperty(
                    propertiId = propertiAktif.id,
                    currentUserId = currentUserId,
                    emitSystemNotifications = emitSystem,
                )

                // 2. Bangun peringatan aktif (setup & kuota penghuni)
                val items = mutableListOf<AppNotification>()
                if (isGlobalRole && propertiAktif.status != "aktif") {
                    items.add(
                        AppNotification(
                            id = "setup_${propertiAktif.id}",
                            type = NotificationType.INFO,
                            title = "Setup properti ${propertiAktif.nama} belum lengkap",
                            subtitle = "Status properti masih ${propertiAktif.status}.",
                            penghuniId = null,
                        ),
                    )
                }
                when (val res = repository.fetchBeranda(null)) {
                    is ApiResult.Success -> {
                        res.data.daftarPeringatan.forEach { p ->
                            val isLewat = p.kuota.status == "lewat"
                            items.add(
                                AppNotification(
                                    id = "kuota_${propertiAktif.id}_${p.penghuniId}_${p.kuota.status}",
                                    type = if (isLewat) NotificationType.DANGER else NotificationType.WARN,
                                    title = if (isLewat) {
                                        "${p.nama} (Kamar ${p.kamarNomor}) melewati kuota"
                                    } else {
                                        "${p.nama} (Kamar ${p.kamarNomor}) mendekati kuota"
                                    },
                                    subtitle = if (isLewat) {
                                        "Pemakaian ${p.kuota.terpakaiKg}/${p.kuota.kuotaKg} kg · Tagihan ${Formatters.formatRupiah(p.kuota.tagihanLewatKuota)}"
                                    } else {
                                        "Pemakaian ${p.kuota.terpakaiKg}/${p.kuota.kuotaKg} kg · Sisa ${p.kuota.sisaKg} kg"
                                    },
                                    penghuniId = p.penghuniId,
                                ),
                            )
                        }
                    }
                    is ApiResult.Failure -> Unit
                }
                _liveAlerts.value = items
            } finally {
                isSyncing = false
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationStore.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        val ids = notifications.value.map { it.id }
        viewModelScope.launch {
            notificationStore.markAllAsRead(ids)
            SystemNotificationHelper.cancelAll(appContext)
        }
    }

    fun clearAll() {
        val ids = notifications.value.map { it.id }
        viewModelScope.launch {
            notificationStore.clearAll(propertiAktif.id, ids)
            SystemNotificationHelper.cancelAll(appContext)
        }
    }

    class Factory(
        private val repository: LaundryKosRepository,
        private val appContext: Context,
        private val notificationStore: NotificationStore,
        private val activitySyncEngine: ActivitySyncEngine,
        private val propertiAktif: PropertiDto,
        private val currentUserId: Int,
        private val isGlobalRole: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotifikasiViewModel(
                repository = repository,
                appContext = appContext,
                notificationStore = notificationStore,
                activitySyncEngine = activitySyncEngine,
                propertiAktif = propertiAktif,
                currentUserId = currentUserId,
                isGlobalRole = isGlobalRole,
            ) as T
    }

    private companion object {
        const val POLL_INTERVAL_MS = 30_000L
    }
}

private val TIME_ONLY_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("id-ID"))
private val DATE_TIME_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.forLanguageTag("id-ID"))

private fun StoredNotification.toAppNotification(isRead: Boolean): AppNotification {
    val parsedType = when (type) {
        "ACTIVITY" -> NotificationType.ACTIVITY
        "DANGER" -> NotificationType.DANGER
        "WARN" -> NotificationType.WARN
        else -> NotificationType.INFO
    }
    val zoned = Instant.ofEpochMilli(timestampMs).atZone(DateUtil.ZONE_JAKARTA)
    val today = LocalDate.now(DateUtil.ZONE_JAKARTA)
    val timeLabel = if (zoned.toLocalDate() == today) {
        "Hari ini · ${zoned.format(TIME_ONLY_FMT)}"
    } else {
        zoned.format(DATE_TIME_FMT)
    }
    return AppNotification(
        id = id,
        type = parsedType,
        title = title,
        subtitle = subtitle,
        penghuniId = penghuniId,
        laundryId = laundryId,
        timestampLabel = timeLabel,
        isRead = isRead,
    )
}

@Composable
fun NotificationBellButton(
    count: Int,
    tint: Color,
    onClick: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    Badge(
                        containerColor = ext.danger,
                        contentColor = Color.White,
                    ) {
                        Text(if (count > 99) "99+" else count.toString())
                    }
                }
            },
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "Notifikasi",
                tint = tint,
            )
        }
    }
}

@Composable
fun NotificationListDialog(
    notifications: List<AppNotification>,
    emptyMessage: String = "Belum ada aktivitas terbaru atau peringatan kuota saat ini.",
    onItemClick: (AppNotification) -> Unit,
    onMarkAllRead: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null,
    onSeeAllPenghuni: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
    val unreadCount = notifications.count { !it.isRead }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notifikasi")
                StatusBadge(
                    text = if (unreadCount > 0) "$unreadCount baru" else "${notifications.size} item",
                    containerColor = if (unreadCount > 0) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    } else {
                        ext.sunken
                    },
                    contentColor = if (unreadCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        ext.muted
                    },
                )
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.muted,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onMarkAllRead != null || onClearAll != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (onMarkAllRead != null) {
                                TextButton(
                                    onClick = onMarkAllRead,
                                    enabled = unreadCount > 0,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tandai dibaca",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            if (onClearAll != null) {
                                TextButton(
                                    onClick = onClearAll,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = ext.danger,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Bersihkan semua",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = ext.danger,
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = ext.line)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            val (bg, fg, icon) = when (notif.type) {
                                NotificationType.ACTIVITY -> Triple(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.primary,
                                    Icons.Default.LocalLaundryService,
                                )
                                NotificationType.DANGER -> Triple(ext.dangerContainer, ext.danger, Icons.Default.WarningAmber)
                                NotificationType.WARN -> Triple(ext.goldContainer, ext.warn, Icons.Default.ErrorOutline)
                                NotificationType.INFO -> Triple(ext.infoContainer, ext.info, Icons.Default.Tune)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (!notif.isRead) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                                        } else {
                                            Color.Transparent
                                        },
                                    )
                                    .clickable { onItemClick(notif) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = bg,
                                    modifier = Modifier.size(34.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                                            ),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (!notif.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                            )
                                        }
                                    }
                                    Text(
                                        text = notif.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.muted,
                                    )
                                    if (!notif.timestampLabel.isNullOrBlank()) {
                                        Text(
                                            text = notif.timestampLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ext.muted.copy(alpha = 0.78f),
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = ext.line)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (onSeeAllPenghuni != null && notifications.isNotEmpty()) {
                TextButton(onClick = onSeeAllPenghuni) {
                    Text("Lihat Semua Penghuni →")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        },
        dismissButton = {
            if (onSeeAllPenghuni != null && notifications.isNotEmpty()) {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        },
    )
}

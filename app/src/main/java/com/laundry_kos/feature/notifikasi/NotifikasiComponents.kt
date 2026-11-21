package com.laundry_kos.feature.notifikasi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
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
import com.laundry_kos.core.util.Formatters
import com.laundry_kos.data.dto.PropertiDto
import com.laundry_kos.data.repository.LaundryKosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NotificationType {
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
)

class NotifikasiViewModel(
    private val repository: LaundryKosRepository,
    private val propertiAktif: PropertiDto,
    private val isGlobalRole: Boolean,
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
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
                                id = "kuota_${p.penghuniId}",
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
            _notifications.value = items
        }
    }

    class Factory(
        private val repository: LaundryKosRepository,
        private val propertiAktif: PropertiDto,
        private val isGlobalRole: Boolean,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotifikasiViewModel(repository, propertiAktif, isGlobalRole) as T
    }
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
    emptyMessage: String = "Belum ada peringatan kuota atau notifikasi baru saat ini.",
    onItemClick: (AppNotification) -> Unit,
    onSeeAllPenghuni: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val ext = LaundryKosTheme.extendedColors
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
                    text = "${notifications.size} item",
                    containerColor = ext.sunken,
                    contentColor = ext.muted,
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        val (bg, fg, icon) = when (notif.type) {
                            NotificationType.DANGER -> Triple(ext.dangerContainer, ext.danger, Icons.Default.WarningAmber)
                            NotificationType.WARN -> Triple(ext.goldContainer, ext.warn, Icons.Default.ErrorOutline)
                            NotificationType.INFO -> Triple(ext.infoContainer, ext.info, Icons.Default.Tune)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(notif) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = notif.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.muted,
                                )
                            }
                        }
                        HorizontalDivider(color = ext.line)
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

package com.laundry_kos.core.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.laundry_kos.R

enum class KuotaVisualStatus {
    NORMAL,
    MENDEKATI,
    LEWAT;

    companion object {
        fun fromApi(raw: String?): KuotaVisualStatus = when (raw?.lowercase()) {
            "lewat" -> LEWAT
            "mendekati" -> MENDEKATI
            else -> NORMAL
        }
    }
}

@Composable
fun KuotaProgressBar(
    persen: Int,
    status: KuotaVisualStatus,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    val fillColor = when (status) {
        KuotaVisualStatus.NORMAL -> ext.ok
        KuotaVisualStatus.MENDEKATI -> ext.warn
        KuotaVisualStatus.LEWAT -> ext.danger
    }
    val clampedFraction = (persen.coerceIn(0, 100)) / 100f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(ext.sunken),
    ) {
        if (clampedFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedFraction)
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(fillColor),
            )
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun LaundryCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, LaundryKosTheme.extendedColors.line),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun MaroonPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun LoadMoreButton(
    ditampilkan: Int,
    total: Int,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LaundryKosTheme.extendedColors.lineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "${stringResource(R.string.aksi_lihat_lebih_banyak)} ($ditampilkan/$total)",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun ErrorBannerCard(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    LaundryCard(
        modifier = modifier,
        containerColor = ext.dangerContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = ext.maroonDarker,
            )
            if (onRetry != null) {
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ext.danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.danger),
                ) {
                    Text(stringResource(R.string.aksi_coba_lagi))
                }
            }
        }
    }
}

@Composable
fun LaundrySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val ext = LaundryKosTheme.extendedColors
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { data ->
        val msg = data.visuals.message
        val isError = msg.contains("gagal", ignoreCase = true) ||
            msg.contains("salah", ignoreCase = true) ||
            msg.contains("tidak", ignoreCase = true) ||
            msg.contains("galat", ignoreCase = true) ||
            msg.contains("belum", ignoreCase = true)

        // Desain cerah dengan transparansi 30% (alpha = 0.70f) dan teks hitam tegas
        val containerColor = if (isError) {
            Color(0xFFFFF1F2).copy(alpha = 0.72f)
        } else {
            Color(0xFFFFFDF9).copy(alpha = 0.70f)
        }
        val borderColor = if (isError) {
            ext.danger.copy(alpha = 0.45f)
        } else {
            ext.lineStrong.copy(alpha = 0.85f)
        }
        val icon = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle
        val iconTint = if (isError) ext.danger else ext.ok
        val textColor = Color(0xFF1A1412)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = containerColor,
            contentColor = textColor,
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = textColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Combobox inline yang bisa diketik langsung untuk menyaring sekaligus menampilkan dropdown pilihan
 * tanpa membuka dialog/form baru.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdownField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onSelect: (T?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Ketik untuk mencari atau pilih...",
    emptyText: String = "Data tidak ditemukan.",
    loading: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
    matchPredicate: (T, String) -> Boolean = { item, query ->
        itemLabel(item).contains(query, ignoreCase = true)
    },
) {
    val ext = LaundryKosTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }
    var isTyping by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(selectedItem?.let(itemLabel) ?: "") }

    LaunchedEffect(selectedItem) {
        if (!isTyping) {
            textValue = selectedItem?.let(itemLabel) ?: ""
        }
    }

    val filtered = remember(items, textValue, isTyping) {
        val q = textValue.trim()
        if (!isTyping || q.isEmpty()) {
            items
        } else {
            items.filter { matchPredicate(it, q) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { next ->
            expanded = next
            if (!next) {
                isTyping = false
                textValue = selectedItem?.let(itemLabel) ?: ""
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { typed ->
                textValue = typed
                isTyping = true
                expanded = true
                val exact = items.firstOrNull { itemLabel(it).equals(typed.trim(), ignoreCase = true) }
                if (exact != null) {
                    onSelect(exact)
                } else if (selectedItem != null) {
                    onSelect(null)
                }
            },
            label = { Text(label) },
            placeholder = { Text(if (loading && items.isEmpty()) "Memuat..." else placeholder) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (textValue.isNotEmpty() || selectedItem != null) {
                        IconButton(
                            onClick = {
                                textValue = ""
                                isTyping = true
                                onSelect(null)
                                expanded = true
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Bersihkan pilihan",
                                modifier = Modifier.size(16.dp),
                                tint = ext.muted,
                            )
                        }
                    }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                isTyping = false
                textValue = selectedItem?.let(itemLabel) ?: ""
            },
            modifier = Modifier
                .heightIn(max = 260.dp)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            if (loading && items.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text("Memuat daftar...", style = MaterialTheme.typography.bodySmall, color = ext.muted)
                        }
                    },
                    onClick = {},
                    enabled = false,
                )
            } else if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(emptyText, style = MaterialTheme.typography.bodySmall, color = ext.muted)
                    },
                    onClick = {},
                    enabled = false,
                )
            } else {
                filtered.forEach { option ->
                    val isSelected = selectedItem != null && itemLabel(selectedItem) == itemLabel(option)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(option),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            textValue = itemLabel(option)
                            isTyping = false
                            expanded = false
                            onSelect(option)
                        },
                    )
                }
            }
        }
    }
}

/**
 * Dropdown inline sederhana (read-only) untuk pilihan singkat seperti Jangka Waktu Sewa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SimpleDropdownField(
    label: String,
    items: List<T>,
    selectedItem: T?,
    itemLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedItem?.let(itemLabel) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        ) {
            items.forEach { option ->
                val isSelected = selectedItem == option
                DropdownMenuItem(
                    text = {
                        Text(
                            text = itemLabel(option),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    LaundryCard(
        modifier = modifier,
        containerColor = LaundryKosTheme.extendedColors.surfaceAlt,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LaundryKosTheme.extendedColors.muted,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LaundryKosComponentsPreview() {
    LaundryKosTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LaundryCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Kamar 307 - Budi", style = MaterialTheme.typography.titleMedium)
                        StatusBadge(
                            text = "Lewat Kuota",
                            containerColor = LaundryKosTheme.extendedColors.dangerContainer,
                            contentColor = LaundryKosTheme.extendedColors.danger,
                        )
                    }
                    KuotaProgressBar(persen = 100, status = KuotaVisualStatus.LEWAT)
                    KuotaProgressBar(persen = 80, status = KuotaVisualStatus.MENDEKATI)
                    KuotaProgressBar(persen = 45, status = KuotaVisualStatus.NORMAL)
                }
            }
            MaroonPrimaryButton(
                text = "Catat Laundry",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
            LoadMoreButton(ditampilkan = 20, total = 108, loading = false, onClick = {})
        }
    }
}

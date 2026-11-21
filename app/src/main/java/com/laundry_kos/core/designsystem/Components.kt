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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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

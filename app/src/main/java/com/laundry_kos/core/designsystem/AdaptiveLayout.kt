package com.laundry_kos.core.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Mengembalikan true saat layar berada pada orientasi landscape atau lebar >= 600dp (tablet/foldable),
 * sehingga layout dapat beradaptasi (NavigationRail, grid 2 kolom, form max-width).
 */
@Composable
fun rememberIsWideLayout(): Boolean {
    val config = LocalConfiguration.current
    return config.screenWidthDp >= 600 || config.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/**
 * Baris bergaya struk/laporan dengan garis titik-titik penghubung antara teks kiri dan kanan
 * (seperti kartu laporan web & aplikasi lama).
 */
@Composable
fun DottedLeaderRow(
    leftText: String,
    rightText: String,
    modifier: Modifier = Modifier,
    leftColor: Color = LaundryKosTheme.extendedColors.muted,
    rightColor: Color = MaterialTheme.colorScheme.onSurface,
    leftStyle: TextStyle = MaterialTheme.typography.bodySmall,
    rightStyle: TextStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
    dotColor: Color = LaundryKosTheme.extendedColors.lineStrong,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = leftText,
            style = leftStyle,
            color = leftColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .padding(horizontal = 2.dp),
        ) {
            drawLine(
                color = dotColor,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), 0f),
            )
        }
        Text(
            text = rightText,
            style = rightStyle,
            color = rightColor,
            maxLines = 1,
        )
    }
}

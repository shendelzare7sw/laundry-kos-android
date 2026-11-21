package com.laundry_kos.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val LaundryKosColorScheme = lightColorScheme(
    primary = Maroon,
    onPrimary = SurfaceWhite,
    primaryContainer = MaroonContainer,
    onPrimaryContainer = MaroonDarker,
    secondary = Gold,
    onSecondary = SurfaceWhite,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGold,
    tertiary = StatusInfo,
    onTertiary = SurfaceWhite,
    tertiaryContainer = StatusInfoContainer,
    onTertiaryContainer = Ink,
    background = BgCream,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = SurfaceSunken,
    onSurfaceVariant = Muted,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = SurfaceAlt,
    surfaceContainer = BgCream,
    surfaceContainerHigh = SurfaceSunken,
    surfaceContainerHighest = Line,
    outline = LineStrong,
    outlineVariant = Line,
    error = StatusDanger,
    onError = SurfaceWhite,
    errorContainer = StatusDangerContainer,
    onErrorContainer = MaroonDarker,
)

@Composable
fun LaundryKosTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalLaundryKosColors provides LaundryKosExtendedColors(),
    ) {
        MaterialTheme(
            colorScheme = LaundryKosColorScheme,
            typography = LaundryKosTypography,
            content = content,
        )
    }
}

object LaundryKosTheme {
    val extendedColors: LaundryKosExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLaundryKosColors.current
}

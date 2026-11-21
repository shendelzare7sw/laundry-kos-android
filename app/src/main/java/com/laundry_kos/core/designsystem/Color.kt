package com.laundry_kos.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Brand Maroon (resources/css/app.css)
val Maroon = Color(0xFF8B1A1A)
val MaroonDark = Color(0xFF6E1414)
val MaroonDarker = Color(0xFF520E0E)
val MaroonCerah = Color(0xFFA11D1D)
val MaroonStrong = Color(0xFFC42626)
val MaroonContainer = Color(0xFFFCEAEA)

// Brand Gold
val Gold = Color(0xFFC8920E)
val GoldBright = Color(0xFFD4A017)
val GoldSoft = Color(0xFFE8C25A)
val GoldContainer = Color(0xFFFBF1D8)
val OnGold = Color(0xFF6B4E08)

// Neutral & Surfaces
val BgCream = Color(0xFFF9F5F0)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceAlt = Color(0xFFFCFAF6)
val SurfaceSunken = Color(0xFFF1EBE2)
val Line = Color(0xFFECE5DB)
val LineStrong = Color(0xFFE0D7C9)

// Typography
val Ink = Color(0xFF1C1C1C)
val Muted = Color(0xFF6B7280)
val Subtle = Color(0xFF9A938A)

// Semantic Status
val StatusOk = Color(0xFF16A34A)
val StatusOkContainer = Color(0xFFDCFCE7)
val StatusWarn = Color(0xFFCA8A04)
val StatusWarnContainer = Color(0xFFFEF9C3)
val StatusDanger = Color(0xFFDC2626)
val StatusDangerContainer = Color(0xFFFEE2E2)
val StatusInfo = Color(0xFF2563EB)
val StatusInfoContainer = Color(0xFFDBEAFE)
val WhatsAppGreen = Color(0xFF16A34A)

@Immutable
data class LaundryKosExtendedColors(
    val maroonDark: Color = MaroonDark,
    val maroonDarker: Color = MaroonDarker,
    val maroonCerah: Color = MaroonCerah,
    val maroonStrong: Color = MaroonStrong,
    val maroonContainer: Color = MaroonContainer,
    val gold: Color = Gold,
    val goldBright: Color = GoldBright,
    val goldSoft: Color = GoldSoft,
    val goldContainer: Color = GoldContainer,
    val onGold: Color = OnGold,
    val surfaceAlt: Color = SurfaceAlt,
    val sunken: Color = SurfaceSunken,
    val line: Color = Line,
    val lineStrong: Color = LineStrong,
    val muted: Color = Muted,
    val subtle: Color = Subtle,
    val ok: Color = StatusOk,
    val okContainer: Color = StatusOkContainer,
    val warn: Color = StatusWarn,
    val warnContainer: Color = StatusWarnContainer,
    val danger: Color = StatusDanger,
    val dangerContainer: Color = StatusDangerContainer,
    val info: Color = StatusInfo,
    val infoContainer: Color = StatusInfoContainer,
    val whatsapp: Color = WhatsAppGreen,
)

val LocalLaundryKosColors = staticCompositionLocalOf { LaundryKosExtendedColors() }

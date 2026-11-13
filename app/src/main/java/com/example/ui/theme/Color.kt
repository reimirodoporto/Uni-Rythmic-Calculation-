package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// 60% Dominant Obsidian Vacuum Chamber & Brass Bezels
val ObsidianVacuumBg = Color(0xFF090A0E)
val DarkBrassBezel = Color(0xFF16130F)
val SlateBronzePanel = Color(0xFF13110E)
val MachinedSlateSurface = Color(0xFF181A20)
val MachinedSlateElevated = Color(0xFF21242D)
val HairlineBrassBorder = Color(0x3DC5A059)
val HairlineBrassStrong = Color(0x7AC5A059)

// 10% Calibrated Incandescent Tungsten & EL-Wire Cyan Accents
val TungstenAmber = Color(0xFFFFAA33)
val TungstenGold = Color(0xFFF59E0B)
val BrassAccent = Color(0xFFC5A059)
val ElWireCyan = Color(0xFF38BDF8)
val CalibrationEmerald = Color(0xFF34D399)
val DivergenceRose = Color(0xFFFB7185)

// Daylight Lab Light Scheme (optional toggle)
val LabLightBg = Color(0xFFF5F3EE)
val LabLightSurface = Color(0xFFFFFFFF)
val LabLightPanel = Color(0xFFECE7DC)
val LabLightBrass = Color(0xFF8C6214)
val LabLightCyan = Color(0xFF0284C7)

val UniRythmicsDarkScheme = darkColorScheme(
    primary = TungstenAmber,
    onPrimary = Color(0xFF1E1200),
    primaryContainer = Color(0xFF36250B),
    onPrimaryContainer = Color(0xFFFFD89C),
    secondary = ElWireCyan,
    onSecondary = Color(0xFF001E2B),
    secondaryContainer = Color(0xFF0A2C3E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = CalibrationEmerald,
    onTertiary = Color(0xFF002114),
    tertiaryContainer = Color(0xFF083827),
    onTertiaryContainer = Color(0xFFA7F3D0),
    error = DivergenceRose,
    onError = Color(0xFF31000C),
    errorContainer = Color(0xFF5A0A1E),
    onErrorContainer = Color(0xFFFFD9DF),
    background = ObsidianVacuumBg,
    onBackground = Color(0xFFF3EFE6),
    surface = DarkBrassBezel,
    onSurface = Color(0xFFF7F3EA),
    surfaceVariant = MachinedSlateSurface,
    onSurfaceVariant = Color(0xFFA7A095),
    surfaceContainerLowest = Color(0xFF06070A),
    surfaceContainerLow = SlateBronzePanel,
    surfaceContainer = MachinedSlateSurface,
    surfaceContainerHigh = MachinedSlateElevated,
    surfaceContainerHighest = Color(0xFF2B2E39),
    outline = Color(0xFF5A4B33),
    outlineVariant = HairlineBrassBorder
)

val UniRythmicsLightScheme = lightColorScheme(
    primary = LabLightBrass,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFAE7C4),
    onPrimaryContainer = Color(0xFF2F1D00),
    secondary = LabLightCyan,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF082F49),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF064E3B),
    error = Color(0xFFE11D48),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE4E6),
    onErrorContainer = Color(0xFF881337),
    background = LabLightBg,
    onBackground = Color(0xFF1A1611),
    surface = LabLightSurface,
    onSurface = Color(0xFF1A1611),
    surfaceVariant = LabLightPanel,
    onSurfaceVariant = Color(0xFF5C5346),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F6F1),
    surfaceContainer = LabLightPanel,
    surfaceContainerHigh = Color(0xFFE2DBCD),
    surfaceContainerHighest = Color(0xFFD7CEBC),
    outline = Color(0xFF9C8D73),
    outlineVariant = Color(0xFFCBBFA8)
)

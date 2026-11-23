package com.mercan.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Material 3'te karşılığı olmayan Seyir renk rolleri. */
@Immutable
data class SeyirColors(
    val selected: Color,
    val onSelected: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
    val warmContainer: Color,
    val onWarmContainer: Color,
)

internal val LightScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDDCFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF7C6299),
    secondaryContainer = Color(0xFFDECAF1),
    surface = Color(0xFFFCF8FF),
    onSurface = Color(0xFF1D1A21),
    surfaceContainerLow = Color(0xFFF7F2FB),
    surfaceContainer = Color(0xFFF1ECF5),
    surfaceContainerHigh = Color(0xFFECE6F1),
    onSurfaceVariant = Color(0xFF4F4559),
    outline = Color(0xFF7F7389),
    outlineVariant = Color(0xFFD6CFDD),
    onSecondary = Color(0xFFFFFFFF),
    onSecondaryContainer = Color(0xFF1D192B),
    surfaceVariant = Color(0xFFE7E0EF),
    background = Color(0xFFFCF8FF),
    onBackground = Color(0xFF1D1A21),
    surfaceContainerHighest = Color(0xFFECE6F1),
    surfaceTint = Color(0xFF6200EE),
)

internal val DarkScheme = darkColorScheme(
    primary = Color(0xFFDBB8FF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF3C245B),
    onPrimaryContainer = Color(0xFFEDDCFF),
    secondary = Color(0xFFAA96BF),
    secondaryContainer = Color(0xFF4D336E),
    surface = Color(0xFF151218),
    onSurface = Color(0xFFE7E0EE),
    surfaceContainerLow = Color(0xFF1B171F),
    surfaceContainer = Color(0xFF221E27),
    surfaceContainerHigh = Color(0xFF2B2630),
    onSurfaceVariant = Color(0xFFCDC2D8),
    outline = Color(0xFF978CA3),
    outlineVariant = Color(0xFF50445D),
    onSecondary = Color(0xFF332D41),
    onSecondaryContainer = Color(0xFFE8DEF8),
    surfaceVariant = Color(0xFF413D4A),
    background = Color(0xFF151218),
    onBackground = Color(0xFFE7E0EE),
    surfaceContainerHighest = Color(0xFF2B2630),
    surfaceTint = Color(0xFFDBB8FF),
)

internal val LightSeyir = SeyirColors(
    selected = Color(0xFF644789),
    onSelected = Color(0xFFFFFFFF),
    positiveContainer = Color(0xFFDCECE2),
    onPositiveContainer = Color(0xFF22604D),
    warmContainer = Color(0xFFF4E7DA),
    onWarmContainer = Color(0xFF78502F),
)

internal val DarkSeyir = SeyirColors(
    selected = Color(0xFFDBB8FF),
    onSelected = Color(0xFF381E72),
    positiveContainer = Color(0xFF203A30),
    onPositiveContainer = Color(0xFFB7DEC9),
    warmContainer = Color(0xFF3B2B22),
    onWarmContainer = Color(0xFFEDC7AA),
)

val LocalSeyirColors = staticCompositionLocalOf { LightSeyir }

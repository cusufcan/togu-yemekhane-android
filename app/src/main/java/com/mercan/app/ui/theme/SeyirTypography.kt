package com.mercan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mercan.app.R

private val Inter = FontFamily(
    Font(R.font.seyir_regular, FontWeight.Normal),
    Font(R.font.seyir_semibold, FontWeight.SemiBold),
    Font(R.font.seyir_bold, FontWeight.Bold),
)

private fun seyirText(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    tracking: Float = 0f,
    numeric: Boolean = false,
) = TextStyle(
    fontFamily = Inter,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    fontFeatureSettings = if (numeric) "tnum" else null,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

internal val SeyirTypography = Typography(
    headlineLarge = seyirText(32, 36, FontWeight.Bold, tracking = -0.02f).copy(lineBreak = LineBreak.Heading),
    headlineSmall = seyirText(28, 32, FontWeight.Bold, numeric = true),
    titleLarge = seyirText(23, 28, FontWeight.Bold, numeric = true),
    titleMedium = seyirText(16, 23, FontWeight.SemiBold),
    titleSmall = seyirText(14, 18, FontWeight.SemiBold),
    bodyLarge = seyirText(16, 23),
    bodyMedium = seyirText(14, 20),
    bodySmall = seyirText(12, 17, numeric = true),
    labelLarge = seyirText(14, 20, FontWeight.SemiBold, numeric = true),
    labelMedium = seyirText(11, 15, FontWeight.Bold),
    labelSmall = seyirText(11, 15, FontWeight.Medium),
)

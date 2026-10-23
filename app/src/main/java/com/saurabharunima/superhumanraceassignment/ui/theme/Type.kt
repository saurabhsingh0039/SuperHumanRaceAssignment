package com.saurabharunima.superhumanraceassignment.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * To match the reference's rounded look, add Nunito TTFs to res/font and replace with:
 * FontFamily(Font(R.font.nunito_regular), Font(R.font.nunito_bold, FontWeight.Bold))
 */
val AppFontFamily: FontFamily = FontFamily.Default

private fun style(weight: FontWeight, size: Int, line: Int) =
    TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp
    )

val AppTypography = Typography(
    headlineMedium = style(FontWeight.Bold, 28, 34),
    titleLarge = style(FontWeight.Bold, 22, 28),
    titleMedium = style(FontWeight.Bold, 18, 24),
    bodyLarge = style(FontWeight.Normal, 16, 24),
    bodyMedium = style(FontWeight.Normal, 14, 20),
    labelLarge = style(FontWeight.Bold, 16, 20),
    labelMedium = style(FontWeight.Bold, 12, 16),
    labelSmall = style(FontWeight.Bold, 11, 14),
)

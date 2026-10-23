package com.saurabharunima.superhumanraceassignment.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Tokens that Material's ColorScheme has no slot for. Read via AppTheme.colors. */
@Immutable
data class AppColors(
    val headerGradient: List<Color>,
    val primaryGradient: List<Color>,
    val coralGradient: List<Color>,
    val cardBorder: Color,
    val iconTile: Color,
    val muted: Color,
    val badge: Color,
    val gold: Color,
    val gem: Color,
    val leaf: Color,
)

private val LightAppColors = AppColors(
    headerGradient = listOf(Blue700, Blue400),
    primaryGradient = listOf(Blue700, Color(0xFF1B74EA)),
    coralGradient = listOf(CoralTop, CoralBottom),
    cardBorder = CardBorder, iconTile = IconTile, muted = Muted,
    badge = BadgeRed, gold = Gold, gem = Gem, leaf = Leaf,
)

private val DarkAppColors = LightAppColors.copy(
    headerGradient = listOf(Color(0xFF0A3F99), Color(0xFF1469C4)),
    cardBorder = DarkBorder, iconTile = DarkBorder, muted = DarkMuted,
)

private val LightScheme = lightColorScheme(
    primary = Blue700, onPrimary = Color.White,
    secondary = CoralBottom, onSecondary = Color.White,
    background = Background, onBackground = Navy,
    surface = Color.White, onSurface = Navy,
    surfaceVariant = IconTile, onSurfaceVariant = Muted,
    outline = CardBorder, error = BadgeRed,
)

private val DarkScheme = darkColorScheme(
    primary = Blue400, onPrimary = DarkBackground,
    secondary = CoralTop, onSecondary = Color.White,
    background = DarkBackground, onBackground = Color.White,
    surface = DarkSurface, onSurface = Color.White,
    surfaceVariant = DarkBorder, onSurfaceVariant = DarkMuted,
    outline = DarkBorder, error = BadgeRed,
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

@Composable
fun SuperHumanTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

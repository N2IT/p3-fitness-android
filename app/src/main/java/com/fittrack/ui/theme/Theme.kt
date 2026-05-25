package com.fittrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = TextPrimary,
    primaryContainer = ElectricBlueDark,
    secondary = AccentOrange,
    onSecondary = TextPrimary,
    secondaryContainer = AccentOrangeDark,
    tertiary = AccentOrangeLight,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary,
    outline = TextTertiary
)

@Composable
fun FitTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

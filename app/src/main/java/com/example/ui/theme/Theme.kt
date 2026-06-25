package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CarbonColorScheme = darkColorScheme(
    primary = GlowingAmber,
    onPrimary = Color.Black,
    secondary = SoftAmber,
    onSecondary = Color.Black,
    tertiary = GlowGreen,
    onTertiary = Color.Black,
    background = DarkCarbonBg,
    onBackground = TextLightPrimary,
    surface = DarkSurfaceCard,
    onSurface = TextLightPrimary,
    surfaceVariant = DarkSurfaceCardElevated,
    onSurfaceVariant = TextLightSecondary,
    outline = BorderCarbon,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CarbonColorScheme,
        typography = Typography,
        content = content
    )
}

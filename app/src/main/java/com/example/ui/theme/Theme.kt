package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Rc30DarkColorScheme = darkColorScheme(
    primary = NeonMatrixGreen,
    onPrimary = Color(0xFF002112),
    primaryContainer = NeonMatrixGreenDim,
    onPrimaryContainer = NeonMatrixGreen,
    secondary = NeonCyberCyan,
    onSecondary = Color(0xFF00262C),
    secondaryContainer = NeonCyberCyanDim,
    onSecondaryContainer = NeonCyberCyan,
    tertiary = NeonWarningAmber,
    onTertiary = Color(0xFF2B1C00),
    tertiaryContainer = NeonWarningAmberDim,
    onTertiaryContainer = NeonWarningAmber,
    error = NeonDangerRed,
    onError = Color.White,
    errorContainer = NeonDangerRedDim,
    onErrorContainer = NeonDangerRed,
    background = CyberObsidian,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Rc30DarkColorScheme,
        typography = Typography,
        content = content
    )
}

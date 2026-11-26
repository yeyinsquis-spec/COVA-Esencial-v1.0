package com.cova.esencial.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CovaBgDark = Color(0xFF0B0F0E)
val CovaSurfaceDark = Color(0xFF141B19)
val CovaSurfaceElevated = Color(0xFF1C2623)
val CovaGreenPrimary = Color(0xFF22C55E)
val CovaGreenDark = Color(0xFF15803D)
val CovaTextPrimary = Color(0xFFF1F5F9)
val CovaTextSecondary = Color(0xFF94A3B8)
val CovaBorder = Color(0xFF2A3834)
val CovaDanger = Color(0xFFEF4444)

private val CovaDarkColors = darkColorScheme(
    primary = CovaGreenPrimary,
    onPrimary = Color(0xFF052E16),
    primaryContainer = CovaGreenDark,
    onPrimaryContainer = CovaTextPrimary,
    secondary = CovaGreenPrimary,
    background = CovaBgDark,
    onBackground = CovaTextPrimary,
    surface = CovaSurfaceDark,
    onSurface = CovaTextPrimary,
    surfaceVariant = CovaSurfaceElevated,
    onSurfaceVariant = CovaTextSecondary,
    outline = CovaBorder,
    error = CovaDanger
)

@Composable
fun CovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CovaDarkColors,
        content = content
    )
}

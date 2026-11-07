package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLightContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = CyanAccent,
    onSecondary = Color.White,
    secondaryContainer = CyanLightContainer,
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = WarnAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarnAmberSoft,
    onTertiaryContainer = Color(0xFF78350F),
    background = SlateBg,
    onBackground = SlateDark,
    surface = SurfaceWhite,
    onSurface = SlateDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = SlateMuted,
    outline = SlateBorder,
    error = DangerRed,
    errorContainer = DangerRedSoft,
    onError = Color.White,
    onErrorContainer = Color(0xFF7F1D1D)
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary80,
    onPrimary = Color(0xFF003735),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = BackgroundDark,
    onBackground = Color(0xFFF8FAFC),
    surface = SurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = BorderDark,
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onError = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFEE2E2)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize our signature deep teal clinical identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

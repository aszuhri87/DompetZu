package com.example.ui.theme

import android.os.Build
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color.Black,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldContainer,
    secondary = SkyLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = SkyContainer,
    tertiary = AmberWarning,
    background = Slate900,
    surface = Slate800,
    surfaceVariant = Slate700,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Slate200,
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = SkySecondary,
    onSecondary = Color.White,
    secondaryContainer = SkyContainer,
    onSecondaryContainer = OnSkyContainer,
    tertiary = AmberWarning,
    background = Color(0xFFEAEFF5),
    surface = Color.White,
    surfaceVariant = Color(0xFFF8FAFC),
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1),
    error = ExpenseRed
)

@Composable
fun DompetKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored emerald fintech scheme by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

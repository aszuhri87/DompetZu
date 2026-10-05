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
    background = Slate50,
    surface = Color.White,
    surfaceVariant = Slate100,
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate600,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

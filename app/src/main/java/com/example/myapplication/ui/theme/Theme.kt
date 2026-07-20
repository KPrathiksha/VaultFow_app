package com.example.vaultflow.ui.theme

import android.app.Activity
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
    primary = VaultPrimary,
    secondary = VaultSecondary,
    tertiary = VaultSecondary,
    background = VaultBackgroundLight,
    surface = VaultSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = VaultTextDark,
    onSurface = VaultTextDark
)

@Composable
fun VaultFlowTheme(
    darkTheme: Boolean = false, // Locked to Light theme by default as requested!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

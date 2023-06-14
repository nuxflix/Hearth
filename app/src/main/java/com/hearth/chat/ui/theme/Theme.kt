package com.hearth.chat.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = HearthEmber,
    onPrimary = HearthCream,
    secondary = HearthInk,
    onSecondary = HearthCream,
    background = HearthPaper,
    onBackground = HearthInk,
    surface = HearthCream,
    onSurface = HearthInk,
    surfaceVariant = Color(0xFFEDE4D8),
    onSurfaceVariant = HearthMuted,
    outline = HearthLine,
)

private val DarkColors = darkColorScheme(
    primary = HearthEmberSoft,
    onPrimary = HearthNight,
    secondary = HearthCream,
    onSecondary = HearthNight,
    background = HearthNight,
    onBackground = HearthCream,
    surface = Color(0xFF221C18),
    onSurface = HearthCream,
    surfaceVariant = Color(0xFF2A241F),
    onSurfaceVariant = HearthDrawerMuted,
    outline = HearthDrawerLine,
)

@Composable
fun HearthTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HearthTypography,
        content = content,
    )
}

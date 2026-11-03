package com.rayven0421.ashnotes.core.designsystem

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AshEmber,
    onPrimary = AshText,
    primaryContainer = AshEmberSoft,
    onPrimaryContainer = AshText,
    secondary = AshTextMuted,
    onSecondary = AshBase,
    background = AshBase,
    onBackground = AshText,
    surface = AshSurface,
    onSurface = AshText,
    surfaceVariant = AshRaised,
    onSurfaceVariant = AshTextMuted,
    outline = AshLine,
    outlineVariant = AshLine,
    error = AshOver,
    onError = AshText
)

/**
 * AshNotes Dark Theme per DESIGN.md.
 * Force dark theme; no dynamic color allowed.
 */
@Composable
fun AshNotesTheme(
    content: @Composable () -> Unit
) {
    val ashColors = AshColors()
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AshBase.toArgb()
            window.navigationBarColor = AshSurface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalAshColors provides ashColors,
        LocalSpacing provides Spacing()
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = AshTypography,
            shapes = AshShapes,
            content = content
        )
    }
}

object AshNotesTheme {
    val colors: AshColors
        @Composable
        get() = LocalAshColors.current

    val spacing: Spacing
        @Composable
        get() = LocalSpacing.current
}

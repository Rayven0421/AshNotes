package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 4dp Grid Spacing for AshNotes per DESIGN.md Section 4.
 */
@Immutable
data class Spacing(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val default: Dp = 16.dp,
    val screenHorizontal: Dp = 20.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val hairline: Dp = 1.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

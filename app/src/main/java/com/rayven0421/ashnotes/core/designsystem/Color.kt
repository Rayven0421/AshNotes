package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * AshNotes Design System Color Tokens per DESIGN.md Section 2.
 * No hex values should be hardcoded outside this file.
 */
val AshBase = Color(0xFF121116)      // App background
val AshSurface = Color(0xFF1A1922)   // Main containers, bottom bar
val AshRaised = Color(0xFF232130)    // Sheets, menus, focused editor block, inputs
val AshLine = Color(0xFF312E3F)      // Borders & dividers (1dp)
val AshText = Color(0xFFEAE7F1)      // Primary text
val AshTextMuted = Color(0xFFA39FB4) // Secondary text, timestamps
val AshEmber = Color(0xFF875FFF)     // Primary action, selection, live meter
val AshEmberSoft = Color(0x29875FFF) // Accent ~16% alpha over base (selected rows, subtle emphasis)
val AshOver = Color(0xFFE9795A)      // Over-budget & destructive actions
val AshOk = Color(0xFF86B89C)        // Positive / under-budget (muted sage)

@Immutable
data class AshColors(
    val base: Color = AshBase,
    val surface: Color = AshSurface,
    val raised: Color = AshRaised,
    val line: Color = AshLine,
    val text: Color = AshText,
    val textMuted: Color = AshTextMuted,
    val ember: Color = AshEmber,
    val emberSoft: Color = AshEmberSoft,
    val over: Color = AshOver,
    val ok: Color = AshOk,
    val disabledAlpha: Float = 0.38f
)

val LocalAshColors = staticCompositionLocalOf { AshColors() }

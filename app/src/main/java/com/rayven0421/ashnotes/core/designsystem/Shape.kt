package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner shapes for AshNotes per DESIGN.md Section 4.
 * - Inputs, chips, small buttons: 8dp
 * - List rows, editor blocks: 12dp
 * - Bottom sheets, dialogs, menus: 20dp
 * - Budget hero container: 24dp
 */
val AshSmallShape = RoundedCornerShape(8.dp)
val AshMediumShape = RoundedCornerShape(12.dp)
val AshLargeShape = RoundedCornerShape(20.dp)
val AshHeroShape = RoundedCornerShape(24.dp)

val AshShapes = Shapes(
    small = AshSmallShape,
    medium = AshMediumShape,
    large = AshLargeShape
)

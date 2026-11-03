package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Reusable Budget Meter composable per DESIGN.md Section 5.
 * - 6dp-tall track in `ash.line`, fill in `ash.ember` (or `ash.over` if over budget).
 * - Pace tick: small vertical mark showing progress through the budget period.
 */
@Composable
fun BudgetMeter(
    spentMinor: Long,
    totalMinor: Long,
    paceFraction: Float = 0.5f, // progress through period (0.0 to 1.0)
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val isOverBudget = spentMinor > totalMinor
    val rawFraction = if (totalMinor > 0) spentMinor.toFloat() / totalMinor.toFloat() else 0f
    val displayFraction = rawFraction.coerceIn(0f, 1f)

    val animatedFraction by animateFloatAsState(
        targetValue = displayFraction,
        animationSpec = tween(durationMillis = 400),
        label = "BudgetProgress"
    )

    val trackColor = AshLine
    val fillColor = if (isOverBudget) AshOver else AshEmber
    val paceColor = AshTextMuted

    val defaultDescription = if (totalMinor > 0) {
        val percent = (rawFraction * 100).toInt()
        "$percent percent spent"
    } else {
        "No budget set"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics {
                this.contentDescription = contentDescription ?: defaultDescription
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(8.dp)) {
            val width = size.width
            val height = size.height
            val cornerRadius = CornerRadius(height / 2f, height / 2f)

            // 1. Draw track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = Size(width, height),
                cornerRadius = cornerRadius
            )

            // 2. Draw fill
            if (totalMinor > 0 && animatedFraction > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset.Zero,
                    size = Size(width * animatedFraction, height),
                    cornerRadius = cornerRadius
                )
            }

            // 3. Draw pace tick (vertical line at paceFraction)
            if (paceFraction in 0f..1f && totalMinor > 0) {
                val tickX = width * paceFraction
                drawLine(
                    color = paceColor,
                    start = Offset(tickX, 0f),
                    end = Offset(tickX, height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}

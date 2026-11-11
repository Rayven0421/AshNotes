package com.rayven0421.ashnotes.core.designsystem

import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.rayven0421.ashnotes.R
import com.rayven0421.ashnotes.core.common.MoneyUtils

/**
 * Calculates the fill fraction for BudgetMeter (0.0f to 1.0f).
 * Returns 0f for zero budget.
 * Returns 1f for over budget (full track in over state).
 * Returns spent / total clamped to [0f, 1f] for normal budget.
 */
fun calculateBudgetMeterFillFraction(spentMinor: Long, totalMinor: Long): Float {
    if (totalMinor <= 0L) return 0f
    if (spentMinor > totalMinor) return 1f
    val fraction = spentMinor.toDouble() / totalMinor.toDouble()
    return fraction.toFloat().coerceIn(0f, 1f)
}

/**
 * Reusable Budget Meter composable per DESIGN.md Section 5.
 * - Track in `ash.line` (6dp), fill in `ash.ember` (or `ash.over` if over budget with full track).
 * - Pace tick: small vertical mark showing progress through the budget period.
 * - Animates once, respects reduced motion setting.
 * - Accessibility text like "62 percent of ₱5,000.00 spent".
 */
@Composable
fun BudgetMeter(
    spentMinor: Long,
    totalMinor: Long,
    paceFraction: Float = 0.5f,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val isOverBudget = totalMinor > 0L && spentMinor > totalMinor
    val targetFillFraction = calculateBudgetMeterFillFraction(spentMinor, totalMinor)

    val context = LocalContext.current
    val isReducedMotion = remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            ) == 0.0f
        } catch (_: Exception) {
            false
        }
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFillFraction,
        animationSpec = if (isReducedMotion) tween(durationMillis = 0) else tween(durationMillis = 400),
        label = "BudgetProgress"
    )

    val trackColor = AshNotesTheme.colors.line
    val fillColor = if (isOverBudget) AshNotesTheme.colors.over else AshNotesTheme.colors.ember
    val paceColor = AshNotesTheme.colors.textMuted
    val meterHeight = AshNotesTheme.spacing.meterBarHeight // 6dp
    val tickStrokeWidth = AshNotesTheme.spacing.hairline * 2

    val defaultSemantics = if (totalMinor > 0L) {
        val percent = ((spentMinor.toDouble() / totalMinor.toDouble()) * 100).toInt()
        val formattedTotal = MoneyUtils.formatMoney(totalMinor)
        stringResource(R.string.budget_meter_semantics_spending, percent, formattedTotal)
    } else {
        stringResource(R.string.budget_meter_semantics_no_budget)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(meterHeight)
            .semantics {
                this.contentDescription = contentDescription ?: defaultSemantics
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(meterHeight)) {
            val width = size.width
            val height = size.height
            val cornerRadius = CornerRadius(height / 2f, height / 2f)

            // 1. Draw track (6dp line color)
            drawRoundRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = Size(width, height),
                cornerRadius = cornerRadius
            )

            // 2. Draw fill
            if (totalMinor > 0L && animatedFraction > 0f) {
                drawRoundRect(
                    color = fillColor,
                    topLeft = Offset.Zero,
                    size = Size(width * animatedFraction, height),
                    cornerRadius = cornerRadius
                )
            }

            // 3. Draw pace tick (vertical line at paceFraction)
            if (paceFraction in 0f..1f && totalMinor > 0L) {
                val tickX = width * paceFraction
                drawLine(
                    color = paceColor,
                    start = Offset(tickX, 0f),
                    end = Offset(tickX, height),
                    strokeWidth = tickStrokeWidth.toPx()
                )
            }
        }
    }
}

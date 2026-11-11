package com.rayven0421.ashnotes.core.designsystem

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetMeterCalculationTest {

    @Test
    fun calculateBudgetMeterFillFraction_zeroBudget_returnsZero() {
        assertEquals(0.0f, calculateBudgetMeterFillFraction(0L, 0L), 0.001f)
        assertEquals(0.0f, calculateBudgetMeterFillFraction(1500L, 0L), 0.001f)
    }

    @Test
    fun calculateBudgetMeterFillFraction_underBudget_returnsProportion() {
        assertEquals(0.6f, calculateBudgetMeterFillFraction(3000L, 5000L), 0.001f)
    }

    @Test
    fun calculateBudgetMeterFillFraction_exactBudget_returnsOne() {
        assertEquals(1.0f, calculateBudgetMeterFillFraction(5000L, 5000L), 0.001f)
    }

    @Test
    fun calculateBudgetMeterFillFraction_overBudget_returnsFullTrack() {
        assertEquals(1.0f, calculateBudgetMeterFillFraction(8000L, 5000L), 0.001f)
    }
}

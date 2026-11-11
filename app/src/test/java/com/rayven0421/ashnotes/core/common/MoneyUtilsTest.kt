package com.rayven0421.ashnotes.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyUtilsTest {

    @Test
    fun parseToMinorUnits_validInputs_parsesCorrectly() {
        assertEquals(125000L, MoneyUtils.parseToMinorUnits("1250"))
        assertEquals(125050L, MoneyUtils.parseToMinorUnits("1,250.50"))
        assertEquals(125050L, MoneyUtils.parseToMinorUnits("1250.5"))
        assertEquals(50L, MoneyUtils.parseToMinorUnits("0.50"))
        assertEquals(0L, MoneyUtils.parseToMinorUnits("0"))
        assertEquals(125050L, MoneyUtils.parseToMinorUnits("₱1,250.50"))
    }

    @Test
    fun parseToMinorUnits_invalidInputs_returnsNull() {
        assertNull(MoneyUtils.parseToMinorUnits(""))
        assertNull(MoneyUtils.parseToMinorUnits("   "))
        assertNull(MoneyUtils.parseToMinorUnits("-100"))
        assertNull(MoneyUtils.parseToMinorUnits("-1,250.50"))
        assertNull(MoneyUtils.parseToMinorUnits("1250.505")) // 3 decimals (>2 allowed)
        assertNull(MoneyUtils.parseToMinorUnits("abc"))
    }

    @Test
    fun formatMoney_formatsCorrectly() {
        assertEquals("₱1,250.50", MoneyUtils.formatMoney(125050L))
        assertEquals("₱0.00", MoneyUtils.formatMoney(0L))
        assertEquals("-₱300.00", MoneyUtils.formatMoney(-30000L))
    }
}

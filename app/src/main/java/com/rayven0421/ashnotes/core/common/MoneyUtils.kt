package com.rayven0421.ashnotes.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

object MoneyUtils {

    /**
     * Parses a string input into minor units (e.g. centavos for PHP).
     * Accepts inputs like "1250", "1,250.50", "1250.5", "₱1,250.50".
     * Rejects empty/blank inputs, negative numbers, non-numeric strings,
     * or inputs with more decimal places than allowed by maxDecimals (default 2).
     */
    fun parseToMinorUnits(input: String, maxDecimals: Int = 2): Long? {
        val cleaned = input.trim()
            .replace(",", "")
            .replace("₱", "")
            .trim()
        if (cleaned.isEmpty()) return null

        return try {
            val bd = BigDecimal(cleaned)
            if (bd < BigDecimal.ZERO) return null

            val normalized = bd.stripTrailingZeros()
            if (normalized.scale() > maxDecimals) return null

            bd.movePointRight(maxDecimals)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Formats minor units (e.g. Long centavos) as a currency string.
     * e.g. 125050L -> "₱1,250.50"
     */
    fun formatMoney(amountMinor: Long, currencySymbol: String = "₱"): String {
        val absAmount = Math.abs(amountMinor)
        val major = absAmount / 100
        val minor = absAmount % 100
        val formatted = String.format(Locale.getDefault(), "%s%,d.%02d", currencySymbol, major, minor)
        return if (amountMinor < 0) "-$formatted" else formatted
    }

    /**
     * Formats minor units as a plain decimal string suitable for text editing.
     * e.g. 100000L -> "1000.00"
     */
    fun formatForInput(amountMinor: Long): String {
        return BigDecimal(amountMinor).movePointLeft(2).toPlainString()
    }
}

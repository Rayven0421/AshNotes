package com.rayven0421.ashnotes.core.common

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatNoteDate(timestamp: Long): String {
    val date = Date(timestamp)
    val nowCalendar = Calendar.getInstance()
    val currentYear = nowCalendar.get(Calendar.YEAR)

    val noteCalendar = Calendar.getInstance().apply { time = date }
    val noteYear = noteCalendar.get(Calendar.YEAR)

    val pattern = if (noteYear == currentYear) "MMM d" else "MMM d, yyyy"
    val formatter = SimpleDateFormat(pattern, Locale.getDefault())
    return formatter.format(date)
}

/**
 * Returns (startDate, endDate) for the current calendar month containing `nowMs`.
 * `startDate` is the 1st of the month at 00:00:00.000.
 * `endDate` is the last day of the month at 23:59:59.999.
 */
fun getCurrentMonthPeriod(nowMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startDate = cal.timeInMillis

    cal.add(Calendar.MONTH, 1)
    cal.add(Calendar.MILLISECOND, -1)
    val endDate = cal.timeInMillis

    return Pair(startDate, endDate)
}

/**
 * Calculates days left until `endDate` and pace fraction through the period (0.0f to 1.0f).
 */
fun calculatePeriodMetrics(
    startDate: Long,
    endDate: Long,
    nowMs: Long = System.currentTimeMillis()
): Pair<Int, Float> {
    val totalDuration = (endDate - startDate).coerceAtLeast(1L)
    val elapsed = (nowMs - startDate).coerceIn(0L, totalDuration)
    val paceFraction = (elapsed.toDouble() / totalDuration.toDouble()).toFloat().coerceIn(0f, 1f)

    val daysLeft = if (nowMs >= endDate) {
        0
    } else {
        val diffMs = endDate - nowMs
        (diffMs / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    return Pair(daysLeft, paceFraction)
}

/**
 * Formats a timestamp into transaction date headers:
 * "Today", "Yesterday", or "Mon 3 Nov" (or "Mon 3 Nov 2024" for a different year).
 */
fun formatTransactionDateHeader(timestamp: Long, nowMs: Long = System.currentTimeMillis()): String {
    val nowCal = Calendar.getInstance().apply {
        timeInMillis = nowMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val itemCal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffDays = ((nowCal.timeInMillis - itemCal.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()

    return when {
        diffDays == 0 -> "Today"
        diffDays == 1 -> "Yesterday"
        else -> {
            val itemYear = itemCal.get(Calendar.YEAR)
            val nowYear = nowCal.get(Calendar.YEAR)
            val pattern = if (itemYear == nowYear) "EEE d MMM" else "EEE d MMM yyyy"
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
        }
    }
}

/**
 * Formats timestamp for expense date picker input field (e.g. "MMM d, yyyy").
 */
fun formatExpenseDisplayDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

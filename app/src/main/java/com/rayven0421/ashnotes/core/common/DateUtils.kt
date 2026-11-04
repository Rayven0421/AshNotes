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

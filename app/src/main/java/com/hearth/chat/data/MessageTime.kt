package com.hearth.chat.data

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val clockTime = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val monthDay = DateTimeFormatter.ofPattern("MMM d", Locale.US)

fun formatMessageTime(
    epochMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    now: Instant = Instant.now(),
): String {
    val sent = Instant.ofEpochMilli(epochMillis).atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    return when (val day = sent.toLocalDate()) {
        today -> clockTime.format(sent)
        today.minusDays(1) -> "Yesterday"
        else -> monthDay.format(day)
    }
}

fun atLocalTime(
    hour: Int,
    minute: Int,
    dayOffset: Int = 0,
    now: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): Long {
    val date = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().plusDays(dayOffset.toLong())
    return ZonedDateTime.of(date, LocalTime.of(hour, minute), zone).toInstant().toEpochMilli()
}

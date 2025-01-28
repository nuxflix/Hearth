package com.hearth.chat.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ChatTextTest {
    private val zone = ZoneId.of("UTC")
    private val now = Instant.parse("2024-06-12T18:00:00Z")

    @Test
    fun sameDayUsesClockTime() {
        val sent = Instant.parse("2024-06-12T13:18:00Z").toEpochMilli()
        assertEquals("1:18 PM", formatMessageTime(sent, zone, now))
    }

    @Test
    fun yesterdayIsLabeled() {
        val sent = Instant.parse("2024-06-11T23:40:00Z").toEpochMilli()
        assertEquals("Yesterday", formatMessageTime(sent, zone, now))
    }

    @Test
    fun olderDayUsesMonthAndDay() {
        val sent = Instant.parse("2024-06-10T09:00:00Z").toEpochMilli()
        assertEquals("Jun 10", formatMessageTime(sent, zone, now))
    }
}

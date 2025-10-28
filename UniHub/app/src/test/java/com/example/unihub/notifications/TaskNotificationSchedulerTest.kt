package com.example.unihub.notifications

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskNotificationSchedulerTest {

    @Test
    fun `parseDateTime handles ISO instant`() {
        val zoneId = ZoneId.of("UTC")
        val zoned = TaskNotificationScheduler.parseDateTime("2024-05-10T12:00:00Z", zoneId)
        requireNotNull(zoned)
        assertEquals(2024, zoned.year)
        assertEquals(5, zoned.monthValue)
        assertEquals(10, zoned.dayOfMonth)
        assertEquals(12, zoned.hour)
        assertEquals(0, zoned.minute)
    }

    @Test
    fun `computeTriggerMillis returns null for past deadlines`() {
        val now = ZonedDateTime.now()
        val past = now.minusHours(2).toString()
        val trigger = TaskNotificationScheduler.computeTriggerMillis(past, now = now)
        assertNull(trigger)
    }

    @Test
    fun `computeTriggerMillis returns epoch millis for future deadlines`() {
        val now = ZonedDateTime.now()
        val future = now.plusHours(6)
        val trigger = TaskNotificationScheduler.computeTriggerMillis(future.toString(), now = now) ?: error("trigger should not be null")
        assertEquals(future.toInstant().toEpochMilli(), trigger)
    }
}
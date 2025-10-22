package com.example.unihub.notifications

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AttendanceNotificationSchedulerTest {

    @Test
    fun `quarta-feira label is mapped to Wednesday`() {
        val mapped = AttendanceNotificationScheduler.mapDayOfWeekInternal("quarta-feira")
        assertEquals(DayOfWeek.WEDNESDAY, mapped)
    }

    @Test
    fun `label normalization strips punctuation and accents`() {
        val normalized = AttendanceNotificationScheduler.normalizeDayLabel("   QUINTA-FEIRÁ  ")
        assertEquals("quintafeira", normalized)
    }

    @Test
    fun `unknown label returns null`() {
        val mapped = AttendanceNotificationScheduler.mapDayOfWeekInternal("dia-misterioso")
        assertNull(mapped)
    }
}
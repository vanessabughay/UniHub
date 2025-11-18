package com.example.unihub.notifications

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrequenciaNotificationSchedulerTest {

    @Test
    fun `quarta-feira label is mapped to Wednesday`() {
        val mapped = FrequenciaNotificationScheduler.mapDayOfWeekInternal("quarta-feira")
        assertEquals(DayOfWeek.WEDNESDAY, mapped)
    }

    @Test
    fun `label normalization strips punctuation and accents`() {
        val normalized = FrequenciaNotificationScheduler.normalizeDayLabel("   QUINTA-FEIRÁ  ")
        assertEquals("quintafeira", normalized)
    }

    @Test
    fun `unknown label returns null`() {
        val mapped = FrequenciaNotificationScheduler.mapDayOfWeekInternal("dia-misterioso")
        assertNull(mapped)
    }
}
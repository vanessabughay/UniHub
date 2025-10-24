package com.example.unihub.notifications

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvaluationNotificationSchedulerTest {

    @Test
    fun `computeTriggerMillis returns timestamp 24 hours before evaluation`() {
        val zone = ZoneId.of("UTC")
        val evaluationDateTime = ZonedDateTime.of(2024, 1, 10, 12, 0, 0, 0, zone)
        val now = evaluationDateTime.minusDays(3)

        val triggerMillis = EvaluationNotificationScheduler.computeTriggerMillis(
            evaluationDateTime.toString(),
            zone,
            now
        )

        val expectedMillis = evaluationDateTime.minusHours(24).toInstant().toEpochMilli()
        assertEquals(expectedMillis, triggerMillis)
    }

    @Test
    fun `computeTriggerMillis returns null when reminder is in the past`() {
        val zone = ZoneId.of("UTC")
        val evaluationDateTime = ZonedDateTime.of(2024, 5, 20, 9, 30, 0, 0, zone)
        val now = evaluationDateTime.minusHours(6)

        val triggerMillis = EvaluationNotificationScheduler.computeTriggerMillis(
            evaluationDateTime.toString(),
            zone,
            now
        )

        assertNull(triggerMillis)
    }
}
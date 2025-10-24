package com.example.unihub.notifications

import com.example.unihub.data.model.Prioridade
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EvaluationNotificationSchedulerTest {

    @Test
    fun `computeTriggerMillis returns timestamp according to priority`() {
        val zone = ZoneId.of("UTC")
        val evaluationDateTime = ZonedDateTime.of(2024, 1, 10, 12, 0, 0, 0, zone)
        val now = evaluationDateTime.minusDays(3)

        val triggerMillis = EvaluationNotificationScheduler.computeTriggerMillis(
            evaluationDateTime.toString(),
            Prioridade.MEDIA,
            zone,
            now
        )

        val expectedMillis = evaluationDateTime.minusHours(48).toInstant().toEpochMilli()
        assertEquals(expectedMillis, triggerMillis)
    }

    @Test
    fun `computeTriggerMillis returns null when reminder is in the past`() {
        val zone = ZoneId.of("UTC")
        val evaluationDateTime = ZonedDateTime.of(2024, 5, 20, 9, 30, 0, 0, zone)
        val now = evaluationDateTime.minusHours(6)

        val triggerMillis = EvaluationNotificationScheduler.computeTriggerMillis(
            evaluationDateTime.toString(),
            Prioridade.MUITO_BAIXA,
            zone,
            now
        )

        assertNull(triggerMillis)
    }

    @Test
    fun `computeTriggerMillis handles highest priority`() {
        val zone = ZoneId.of("UTC")
        val evaluationDateTime = ZonedDateTime.of(2024, 8, 15, 8, 0, 0, 0, zone)
        val now = evaluationDateTime.minusDays(10)

        val triggerMillis = EvaluationNotificationScheduler.computeTriggerMillis(
            evaluationDateTime.toString(),
            Prioridade.MUITO_ALTA,
            zone,
            now
        )

        val expectedMillis = evaluationDateTime.minusDays(7).toInstant().toEpochMilli()
        assertEquals(expectedMillis, triggerMillis)
    }
}
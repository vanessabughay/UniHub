package com.example.unihub.notifications

import java.time.ZoneId

import java.time.ZonedDateTime

import org.junit.Assert.assertEquals

import org.junit.Assert.assertNull

import org.junit.Assert.assertNotNull

import org.junit.Test

class TaskNotificationSchedulerTest {

    @Test

    fun `computeTriggerMillis returns deadline timestamp when it is in the future`() {

        val zone = ZoneId.of("UTC")

        val deadline = ZonedDateTime.of(2024, 10, 5, 18, 30, 0, 0, zone)

        val now = deadline.minusHours(2)

        val triggerMillis = TaskNotificationScheduler.computeTriggerMillis(

            deadline.toString(),

            zone,

            now

        )

        assertEquals(deadline.toInstant().toEpochMilli(), triggerMillis)

    }

    @Test

    fun `computeTriggerMillis returns null when deadline already passed`() {

        val zone = ZoneId.of("UTC")

        val deadline = ZonedDateTime.of(2024, 10, 5, 18, 30, 0, 0, zone)

        val now = deadline.plusMinutes(1)

        val triggerMillis = TaskNotificationScheduler.computeTriggerMillis(

            deadline.toString(),

            zone,

            now

        )

        assertNull(triggerMillis)

    }

    @Test

    fun `computeTriggerMillis allows scheduling exactly at the current instant`() {

        val zone = ZoneId.of("UTC")

        val deadline = ZonedDateTime.of(2024, 10, 5, 18, 30, 0, 0, zone)

        val now = deadline

        val triggerMillis = TaskNotificationScheduler.computeTriggerMillis(

            deadline.toString(),

            zone,

            now

        )

        assertNotNull(triggerMillis)

        assertEquals(deadline.toInstant().toEpochMilli(), triggerMillis)

    }

}
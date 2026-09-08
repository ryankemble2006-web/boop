package com.boop.shieldturbo.cleanstart

import org.junit.Assert.*
import org.junit.Test

class CleanStartSchedulerTest {
    @Test fun retriesAreFiniteAndBackOff() {
        assertEquals(30_000L, CleanStartScheduler.delayForAttempt(0))
        assertEquals(60_000L, CleanStartScheduler.delayForAttempt(1))
        assertEquals(120_000L, CleanStartScheduler.delayForAttempt(2))
        assertNull(CleanStartScheduler.delayForAttempt(3))
        assertNull(CleanStartScheduler.delayForAttempt(-1))
    }

    @Test fun bootSchedulingRequiresExplicitAutoAndTargets() {
        assertFalse(CleanStartScheduler.shouldSchedule(autoEnabled = false, targetCount = 4))
        assertFalse(CleanStartScheduler.shouldSchedule(autoEnabled = true, targetCount = 0))
        assertTrue(CleanStartScheduler.shouldSchedule(autoEnabled = true, targetCount = 1))
    }
}

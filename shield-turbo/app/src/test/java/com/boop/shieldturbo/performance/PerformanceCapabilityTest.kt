package com.boop.shieldturbo.performance

import org.junit.Assert.assertEquals
import org.junit.Test

class PerformanceCapabilityTest {
    @Test fun thermalLabelsAreStable() {
        assertEquals("Not exposed", thermalLabel(null))
        assertEquals("None", thermalLabel(0))
        assertEquals("Light", thermalLabel(1))
        assertEquals("Moderate", thermalLabel(2))
        assertEquals("Severe", thermalLabel(3))
        assertEquals("Critical", thermalLabel(4))
        assertEquals("Emergency", thermalLabel(5))
        assertEquals("Shutdown", thermalLabel(6))
    }
}

package com.boop.shieldturbo.system

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickCheckTest {
    @Test fun lowStorageWarnsBelowFivePercent() {
        assertEquals(Level.WARNING, QuickCheck.storage(4, 100).level)
    }

    @Test fun healthyStorageDoesNotPretendToBoost() {
        assertEquals(Level.OK, QuickCheck.storage(20, 100).level)
    }

    @Test fun unknownCapacityDoesNotInventAWarning() {
        assertEquals(Level.OK, QuickCheck.storage(0, 0).level)
    }
}

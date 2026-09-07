package com.boop.shieldturbo

import org.junit.Assert.assertEquals
import org.junit.Test

class BrightnessTest {
    @Test fun clampsBrightnessToSafeRange() {
        assertEquals(10, Brightness.clampPercent(-20))
        assertEquals(10, Brightness.clampPercent(0))
        assertEquals(55, Brightness.clampPercent(55))
        assertEquals(100, Brightness.clampPercent(140))
    }

    @Test fun convertsPercentToDimAlpha() {
        assertEquals(0f, Brightness.dimAlpha(100), 0.0001f)
        assertEquals(0.5f, Brightness.dimAlpha(50), 0.0001f)
        assertEquals(0.9f, Brightness.dimAlpha(10), 0.0001f)
    }
}

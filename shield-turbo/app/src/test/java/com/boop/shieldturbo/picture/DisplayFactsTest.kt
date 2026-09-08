package com.boop.shieldturbo.picture

import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayFactsTest {
    @Test fun formatsActiveModeWithoutPretendingToChangeIt() {
        assertEquals("3840x2160 @ 59.94 Hz", DisplayFacts.formatMode(3840, 2160, 59.94f))
    }

    @Test fun missingModeIsPlainEnglish() {
        assertEquals("Not exposed", DisplayFacts.formatMode(0, 0, 0f))
    }
}

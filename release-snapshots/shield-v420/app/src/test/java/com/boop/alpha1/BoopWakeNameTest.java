package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BoopWakeNameTest {
    @Test public void defaultWakeNameIsBoop() {
        assertEquals("BOOP", BoopWakeName.DEFAULT);
        assertEquals("BOOP", BoopWakeName.normalize(null));
        assertEquals("BOOP", BoopWakeName.normalize("   "));
        assertTrue(BoopWakeName.isDefault("boop"));
    }

    @Test public void customNameNormalizesWithoutChangingBoopDefault() {
        assertEquals("Sir Boopington", BoopWakeName.normalize("  Sir   Boopington  "));
        assertFalse(BoopWakeName.isDefault("Steve"));
        assertEquals("BOOP", BoopWakeName.DEFAULT);
    }
}

package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopToastEggTest {
    @Test
    public void triggerIsDeliberatelyNarrow() {
        assertTrue(BoopToastEgg.matches("toast"));
        assertTrue(BoopToastEgg.matches("  TOAST  "));
        assertFalse(BoopToastEgg.matches("make toast"));
        assertFalse(BoopToastEgg.matches("pause"));
        assertFalse(BoopToastEgg.matches(null));
    }

    @Test
    public void repeatedUseEscalatesThenWrapsBackToPolite() {
        BoopToastEgg egg = new BoopToastEgg();

        BoopToastEgg.Moment first = egg.next();
        assertEquals(0, first.level());
        assertEquals("Would you like some toast?", first.line());

        BoopToastEgg.Moment second = egg.next();
        assertEquals(1, second.level());
        assertNotEquals(first.line(), second.line());

        BoopToastEgg.Moment latest = second;
        for (int level = 2; level < BoopToastEgg.CYCLE_LENGTH; level++) {
            latest = egg.next();
            assertEquals(level, latest.level());
            assertFalse(latest.line().trim().isEmpty());
        }
        assertTrue(latest.line().contains("TOAST EMERGENCY"));

        BoopToastEgg.Moment wrapped = egg.next();
        assertEquals(0, wrapped.level());
        assertEquals(first.line(), wrapped.line());
    }
}

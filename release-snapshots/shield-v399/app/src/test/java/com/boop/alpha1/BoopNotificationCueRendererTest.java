package com.boop.alpha1;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopNotificationCueRendererTest {
    @Test
    public void cueIsShortDeterministicAndBounded() {
        short[] a = BoopNotificationCueRenderer.render(44100);
        short[] b = BoopNotificationCueRenderer.render(44100);

        assertEquals(14112, a.length);
        assertArrayEquals(a, b);
        int peak = 0;
        for (short sample : a) {
            peak = Math.max(peak, Math.abs((int) sample));
        }
        assertTrue(peak > 2000 && peak <= 32767);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSampleRateIsRejected() {
        BoopNotificationCueRenderer.render(0);
    }
}

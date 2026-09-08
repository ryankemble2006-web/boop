package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

/** Synthetic colour arithmetic only; no screenshots or artwork acceptance. */
public final class BoopIrisTintMathTest {
    @Test public void defaultKeepsOriginalPixelExactly() {
        assertEquals(0xff35cfff, BoopIrisTintMath.tint(0xff35cfff, 190));
    }
    @Test public void neutralAndTransparentPixelsStayUntouched() {
        for (int pixel : new int[]{0xffffffff, 0xffced6df, 0xff000000, 0xff222222, 0x0035cfff}) {
            assertEquals(pixel, BoopIrisTintMath.tint(pixel, 10));
        }
    }
    @Test public void blueChangesButAlphaAndOtherHuesDoNot() {
        int pixel = 0x8035cfff;
        int changed = BoopIrisTintMath.tint(pixel, 300);
        assertNotEquals(pixel, changed);
        assertEquals(pixel >>> 24, changed >>> 24);
        assertEquals(0xffffdd00, BoopIrisTintMath.tint(0xffffdd00, 300));
    }
    @Test public void onlyIrisRingCanBeSelected() {
        assertTrue(BoopIrisTintMath.inIris(290, 933, 941, 1672));
        assertTrue(BoopIrisTintMath.inIris(657, 933, 941, 1672));
        assertFalse(BoopIrisTintMath.inIris(290, 842, 941, 1672));
        assertFalse(BoopIrisTintMath.inIris(657, 842, 941, 1672));
        assertFalse(BoopIrisTintMath.inIris(120, 840, 941, 1672));
        assertFalse(BoopIrisTintMath.inIris(300, 670, 941, 1672));
        assertFalse(BoopIrisTintMath.inIris(290, 843, 0, 0));
    }
}

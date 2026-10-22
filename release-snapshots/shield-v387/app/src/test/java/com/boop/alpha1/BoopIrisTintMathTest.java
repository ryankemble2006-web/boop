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

    @Test public void darkApprovedIrisBlueAlsoChangesButNeutralScleraDoesNot() {
        int darkUpperIris = 0xff07111e;
        assertNotEquals(darkUpperIris, BoopIrisTintMath.tint(darkUpperIris, 120));
        assertEquals(0xffc0d8e9, BoopIrisTintMath.tint(0xffc0d8e9, 120));
    }

    @Test public void onlyApprovedIrisRingsCanBeSelected() {
        // Exact approved-master iris centres are slightly right/down of the old mask.
        assertTrue(BoopIrisTintMath.inIris(535, 340, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(733, 543, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(1233, 340, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(1431, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(535, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(1233, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(100, 543, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(535, 100, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(535, 543, 0, 0));
    }
}

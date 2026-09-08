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

    @Test public void onlyApprovedIrisRingsCanBeSelected() {
        assertTrue(BoopIrisTintMath.inIris(522, 709, 1774, 887));
        assertTrue(BoopIrisTintMath.inIris(1204, 709, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(522, 529, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(1204, 529, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(100, 529, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(522, 100, 1774, 887));
        assertFalse(BoopIrisTintMath.inIris(522, 529, 0, 0));
    }
}

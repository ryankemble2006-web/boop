package com.boop.shieldhome;

import java.lang.reflect.Method;

/** Numeric geometry checks only; device visual acceptance remains separate. */
public final class ArtworkFrameGeometryHarness {
    public static void main(String[] args) throws Exception {
        Method radius;
        try {
            radius = FocusChrome.class.getDeclaredMethod(
                    "artworkStrokeRadius", float.class, float.class);
        } catch (NoSuchMethodException missing) {
            throw new AssertionError("Artwork stroke does not compensate its half-width inset", missing);
        }
        if (FocusChrome.ARTWORK_BORDER_DP != 4 || FocusChrome.BORDER_DP != 4)
            throw new AssertionError("Restore thin artwork frame; leave normal chrome at 4 dp");
        int cases = 0;
        for (float density : new float[]{0.75f, 1f, 1.33125f, 1.5f, 2f, 3f}) {
            for (int cornerDp : new int[]{8, 10}) {
                float outer = Math.round(cornerDp * density);
                float stroke = Math.round(4 * density);
                float centre = (Float) radius.invoke(null, outer, stroke);
                close(outer, centre + stroke / 2f, "Outer corner radius");
                close(outer, stroke / 2f + centre, "Corner centre alignment");
                cases++;
            }
        }
        close(0f, (Float) radius.invoke(null, 1f, 4f), "Nonnegative radius");
        System.out.println("Artwork corner geometry: " + cases + " density/radius cases passed");
    }
    private static void close(float expected, float actual, String message) {
        if (!Float.isFinite(actual) || Math.abs(expected - actual) > 0.0001f)
            throw new AssertionError(message + ": " + actual + " != " + expected);
    }
}

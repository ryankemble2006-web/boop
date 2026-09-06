package com.boop.shieldoverlay;

import static org.junit.Assert.*;
import org.junit.Test;

public final class HeadphoneGeometryTest {
    @Test public void hdAndUhdKeepEyeSpanAndFullAlphaMotionInsideCorner() {
        for (int[] screen : new int[][] {{1920, 1080}, {3840, 2160}}) {
            HeadphoneGeometry.Layout layout = HeadphoneGeometry.calculate(screen[0], screen[1]);
            assertEquals(screen[0] * .14, 686 * layout.scale, 1.0);
            assertTrue(layout.x >= screen[0] * .03);
            assertTrue(layout.y >= screen[1] * .03);
            assertTrue(layout.width < screen[0] * .25);
            assertTrue(layout.height < screen[1] * .4);
            assertEnvelope(layout);
        }
    }

    @Test public void smallZeroAndInvalidDisplaysHavePositiveBoundedLayout() {
        for (int[] screen : new int[][] {{320,180}, {50,10}, {1,1}, {0,0}, {-1,-100}}) {
            HeadphoneGeometry.Layout layout = HeadphoneGeometry.calculate(screen[0], screen[1]);
            assertTrue(layout.width > 0 && layout.height > 0 && layout.scale > 0);
            assertTrue(layout.x >= 0 && layout.y >= 0);
            assertTrue(layout.width + layout.x <= Math.max(1, screen[0]));
            assertTrue(layout.height + layout.y <= Math.max(1, screen[1]));
            assertEnvelope(layout);
        }
    }

    private static void assertEnvelope(HeadphoneGeometry.Layout layout) {
        // Independent corner transform of measured PNG alpha bounds (including lower shading).
        // Sample every millisecond of the unchanged sampler, plus exact +/-1.8 degree extremes.
        for (long time = 0; time < 3600; time++) {
            MediaPuppetMotion.Pose pose = MediaPuppetMotion.music(time);
            double radians = Math.toRadians(pose.rotationDegrees);
            for (double x : new double[] {218 - 768, 1300 - 768}) {
                for (double y : new double[] {120 - 580, 902 - 580}) {
                    double px = layout.originX + layout.scale *
                            (pose.x + x * Math.cos(radians) - y * Math.sin(radians));
                    double py = layout.originY + layout.scale *
                            (pose.y + x * Math.sin(radians) + y * Math.cos(radians));
                    assertTrue("alpha clipped horizontally", px >= -0.0001 && px <= layout.width);
                    assertTrue("alpha clipped vertically", py >= -0.0001 && py <= layout.height);
                }
            }
        }
    }
}

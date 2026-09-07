package com.boop.shieldoverlay;

import static org.junit.Assert.*;
import org.junit.Test;

public final class FullscreenDeezerGeometryTest {
    @Test public void commonTvModesUseFullCanvasAndKeepPuppetCentred() {
        for (int[] screen : new int[][] {{1280,720}, {1920,1080}, {3840,2160}}) {
            HeadphoneGeometry.Layout layout = FullscreenDeezerGeometry.calculate(screen[0], screen[1]);
            assertEquals(screen[0], layout.width);
            assertEquals(screen[1], layout.height);
            assertEquals(0, layout.x);
            assertEquals(0, layout.y);
            assertEquals(screen[0] / 2f, layout.originX, 1.0f);
            assertTrue(layout.scale > 0f);
            assertEnvelope(layout);
        }
    }

    @Test public void invalidSizesRemainPositiveAndBounded() {
        for (int[] screen : new int[][] {{1,1}, {0,0}, {-1,-100}}) {
            HeadphoneGeometry.Layout layout = FullscreenDeezerGeometry.calculate(screen[0], screen[1]);
            assertTrue(layout.width >= 1);
            assertTrue(layout.height >= 1);
            assertTrue(layout.scale > 0f);
            assertEnvelope(layout);
        }
    }

    private static void assertEnvelope(HeadphoneGeometry.Layout layout) {
        for (long time = 0; time < 3600; time += 8) {
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

package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class FullscreenPuppetMotionTest {
    private static final float EPSILON = 0.001f;

    @Test public void grooveIsRicherButStillGentleAndPeriodic() {
        boolean differsFromOldGroove = false;
        for (long t = 0; t < MediaPuppetMotion.MUSIC_PERIOD_MS; t += 30) {
            MediaPuppetMotion.Pose pose = FullscreenPuppetMotion.groove(t);
            MediaPuppetMotion.Pose old = MediaPuppetMotion.music(t);
            assertTrue(Math.abs(pose.x) <= 10.5f);
            assertTrue(pose.y <= 1f && pose.y >= -14.5f);
            assertTrue(Math.abs(pose.rotationDegrees) <= 2.8f);
            if (Math.abs(pose.x - old.x) > 0.05f
                    || Math.abs(pose.y - old.y) > 0.05f
                    || Math.abs(pose.rotationDegrees - old.rotationDegrees) > 0.05f) {
                differsFromOldGroove = true;
            }
        }
        assertTrue("fullscreen groove should add puppet acting", differsFromOldGroove);
        assertPoseEquals(FullscreenPuppetMotion.groove(731),
                FullscreenPuppetMotion.groove(731 + MediaPuppetMotion.MUSIC_PERIOD_MS));
    }

    @Test public void pauseSettleFinishesAtNeutralWithoutOvershoot() {
        MediaPuppetMotion.Pose start = new MediaPuppetMotion.Pose(8f, -10f, 2.4f, 1f);
        assertPoseEquals(start, FullscreenPuppetMotion.settle(start, 0));
        MediaPuppetMotion.Pose middle = FullscreenPuppetMotion.settle(start,
                FullscreenPuppetMotion.SETTLE_DURATION_MS / 2);
        assertTrue(Math.abs(middle.x) < Math.abs(start.x));
        assertTrue(Math.abs(middle.y) < Math.abs(start.y));
        assertTrue(Math.abs(middle.rotationDegrees) < Math.abs(start.rotationDegrees));
        assertPoseEquals(FullscreenPuppetMotion.rest(),
                FullscreenPuppetMotion.settle(start, FullscreenPuppetMotion.SETTLE_DURATION_MS));
        assertPoseEquals(FullscreenPuppetMotion.rest(),
                FullscreenPuppetMotion.settle(start, FullscreenPuppetMotion.SETTLE_DURATION_MS + 500));
    }

    @Test public void trackChangePerksUpThenReturnsHome() {
        assertPoseEquals(FullscreenPuppetMotion.rest(), FullscreenPuppetMotion.trackChange(0));
        MediaPuppetMotion.Pose perk = FullscreenPuppetMotion.trackChange(180);
        assertTrue("perk should lift", perk.y < -18f);
        assertTrue("perk should tilt", Math.abs(perk.rotationDegrees) > 2f);
        assertPoseEquals(FullscreenPuppetMotion.rest(),
                FullscreenPuppetMotion.trackChange(FullscreenPuppetMotion.TRACK_CHANGE_DURATION_MS));
        assertPoseEquals(FullscreenPuppetMotion.rest(),
                FullscreenPuppetMotion.trackChange(FullscreenPuppetMotion.TRACK_CHANGE_DURATION_MS + 200));
    }

    private static void assertPoseEquals(MediaPuppetMotion.Pose expected,
            MediaPuppetMotion.Pose actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.rotationDegrees, actual.rotationDegrees, EPSILON);
        assertEquals(expected.kernelAlpha, actual.kernelAlpha, EPSILON);
    }
}

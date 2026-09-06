package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.Test;

public final class MediaPuppetMotionTest {
    private static final float EPSILON = 0.0001f;

    @Test
    public void musicStaysWithinItsGentleMotionEnvelope() {
        for (long elapsedMs = 0; elapsedMs < MediaPuppetMotion.MUSIC_PERIOD_MS; elapsedMs += 10) {
            MediaPuppetMotion.Pose pose = MediaPuppetMotion.music(elapsedMs);

            assertTrue("music x at " + elapsedMs, Math.abs(pose.x) <= 6f + EPSILON);
            assertTrue("music y at " + elapsedMs, Math.abs(pose.y) <= 9f + EPSILON);
            assertTrue(
                    "music rotation at " + elapsedMs,
                    Math.abs(pose.rotationDegrees) <= 1.8f + EPSILON);
            assertEquals(1f, pose.kernelAlpha, 0f);
        }
    }

    @Test
    public void musicRepeatsAtTheDeclaredPeriod() {
        assertPoseEquals(MediaPuppetMotion.music(0), MediaPuppetMotion.music(3600));
        assertPoseEquals(MediaPuppetMotion.music(731), MediaPuppetMotion.music(4331));
        assertPoseEquals(MediaPuppetMotion.music(-1), MediaPuppetMotion.music(3599));
    }

    @Test
    public void musicProducesFiniteValuesForExtremeTimes() {
        assertFinite(MediaPuppetMotion.music(-1));
        assertFinite(MediaPuppetMotion.music(Long.MAX_VALUE));
    }

    @Test
    public void cinemaFollowsTheRestLiftFadeReturnAndPickupSequence() {
        assertPoseEquals(pose(0f, 0f, 0f, 1f), MediaPuppetMotion.cinema(0));
        assertPoseEquals(pose(0f, 0f, 0f, 1f), MediaPuppetMotion.cinema(4400));
        assertPoseEquals(pose(52.5f, -92.5f, 4.5f, 1f), MediaPuppetMotion.cinema(5150));
        assertPoseEquals(pose(105f, -185f, 9f, 1f), MediaPuppetMotion.cinema(5900));
        assertPoseEquals(pose(105f, -185f, 9f, 1f), MediaPuppetMotion.cinema(6500));
        assertPoseEquals(pose(105f, -185f, 9f, 0.5f), MediaPuppetMotion.cinema(6600));
        assertPoseEquals(pose(105f, -185f, 9f, 0f), MediaPuppetMotion.cinema(6700));
        assertEquals(0f, MediaPuppetMotion.cinema(7400).kernelAlpha, 0f);
        assertPoseEquals(pose(0f, 0f, 0f, 0f), MediaPuppetMotion.cinema(8900));
        assertPoseEquals(pose(0f, 0f, 0f, 0f), MediaPuppetMotion.cinema(9300));
        assertPoseEquals(pose(0f, 0f, 0f, 0.5f), MediaPuppetMotion.cinema(9375));
        assertPoseEquals(pose(0f, 0f, 0f, 1f), MediaPuppetMotion.cinema(9450));
    }

    @Test
    public void cinemaReturnMovesSmoothlyTowardTheEmptyRestPose() {
        MediaPuppetMotion.Pose returning = MediaPuppetMotion.cinema(8050);

        assertPoseEquals(pose(52.5f, -92.5f, 4.5f, 0f), returning);
    }

    @Test
    public void cinemaRepeatsAtTheDeclaredPeriodAndHandlesExtremeTimes() {
        assertPoseEquals(MediaPuppetMotion.cinema(0), MediaPuppetMotion.cinema(12000));
        assertPoseEquals(MediaPuppetMotion.cinema(5150), MediaPuppetMotion.cinema(17150));
        assertPoseEquals(MediaPuppetMotion.cinema(-1), MediaPuppetMotion.cinema(11999));
        assertFinite(MediaPuppetMotion.cinema(-1));
        assertFinite(MediaPuppetMotion.cinema(Long.MAX_VALUE));
    }

    @Test
    public void poseExposesOnlyFinalValueFields() {
        for (String fieldName : new String[] {"x", "y", "rotationDegrees", "kernelAlpha"}) {
            try {
                Field field = MediaPuppetMotion.Pose.class.getField(fieldName);
                assertEquals(float.class, field.getType());
                assertTrue(fieldName + " must be final", Modifier.isFinal(field.getModifiers()));
            } catch (NoSuchFieldException error) {
                throw new AssertionError("Missing public field " + fieldName, error);
            }
        }
    }

    private static MediaPuppetMotion.Pose pose(
            float x, float y, float rotationDegrees, float kernelAlpha) {
        return new MediaPuppetMotion.Pose(x, y, rotationDegrees, kernelAlpha);
    }

    private static void assertFinite(MediaPuppetMotion.Pose pose) {
        assertTrue(Float.isFinite(pose.x));
        assertTrue(Float.isFinite(pose.y));
        assertTrue(Float.isFinite(pose.rotationDegrees));
        assertTrue(Float.isFinite(pose.kernelAlpha));
    }

    private static void assertPoseEquals(
            MediaPuppetMotion.Pose expected, MediaPuppetMotion.Pose actual) {
        assertEquals(expected.x, actual.x, EPSILON);
        assertEquals(expected.y, actual.y, EPSILON);
        assertEquals(expected.rotationDegrees, actual.rotationDegrees, EPSILON);
        assertEquals(expected.kernelAlpha, actual.kernelAlpha, EPSILON);
    }
}

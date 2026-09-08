package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BackPressGestureTest {
    @Test public void quickReleaseIsShortBack() {
        BackPressGesture gesture = new BackPressGesture();
        gesture.onDown();
        assertTrue(gesture.onUpShouldRunShortBack());
    }

    @Test public void triggeredHoldSuppressesShortBackOnRelease() {
        BackPressGesture gesture = new BackPressGesture();
        gesture.onDown();
        assertTrue(gesture.onHoldTriggered());
        assertFalse(gesture.onUpShouldRunShortBack());
    }

    @Test public void cancelledPressDoesNothing() {
        BackPressGesture gesture = new BackPressGesture();
        gesture.onDown();
        gesture.cancel();
        assertFalse(gesture.onHoldTriggered());
        assertFalse(gesture.onUpShouldRunShortBack());
    }

    @Test public void duplicateHoldOnlyFiresOnce() {
        BackPressGesture gesture = new BackPressGesture();
        gesture.onDown();
        assertTrue(gesture.onHoldTriggered());
        assertFalse(gesture.onHoldTriggered());
    }
}

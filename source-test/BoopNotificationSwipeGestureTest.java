package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BoopNotificationSwipeGestureTest {
    @Test
    public void deliberateSwipeDismissesButTapDoesNot() {
        assertTrue(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 190f, 20f, 2f));
        assertFalse(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 20f, 18f, 2f));
    }

    @Test
    public void diagonalDriftDoesNotCountAsDismiss() {
        assertFalse(BoopNotificationSwipeGesture.isDismiss(0f, 0f, 180f, 150f, 2f));
    }
}

package com.boop.alpha1;

public final class BoopLauncherSwipeGestureHarness {
    public static void main(String[] args) {
        assertTrue(BoopLauncherSwipeGesture.isDeliberateLeftSwipe(500f, 300f, 300f, 320f, 1f));
        assertFalse(BoopLauncherSwipeGesture.isDeliberateLeftSwipe(500f, 300f, 430f, 305f, 1f));
        assertFalse(BoopLauncherSwipeGesture.isDeliberateLeftSwipe(500f, 300f, 290f, 520f, 1f));
        assertFalse(BoopLauncherSwipeGesture.isDeliberateLeftSwipe(300f, 300f, 500f, 300f, 1f));
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected deliberate left swipe");
        }
    }

    private static void assertFalse(boolean value) {
        if (value) {
            throw new AssertionError("Expected gesture to be ignored");
        }
    }
}

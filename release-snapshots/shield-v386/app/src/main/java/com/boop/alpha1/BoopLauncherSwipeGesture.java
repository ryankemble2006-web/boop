package com.boop.alpha1;

public final class BoopLauncherSwipeGesture {
    private static final float DIRECTIONAL_CONFIDENCE = 1.5f;

    private BoopLauncherSwipeGesture() {
    }

    public static boolean shouldOpenLauncher(float downX, float downY, float upX, float upY,
            boolean cancelled, boolean multiTouch, float minLeftDistancePx) {
        if (cancelled || multiTouch) return false;

        float horizontalDistance = downX - upX;
        float verticalDistance = Math.abs(upY - downY);
        return horizontalDistance >= minLeftDistancePx
                && horizontalDistance >= verticalDistance * DIRECTIONAL_CONFIDENCE;
    }
}

package com.boop.alpha1;

final class BoopLauncherSwipeGesture {
    static final float MIN_HORIZONTAL_DISTANCE_DP = 96f;
    static final float DIRECTIONAL_CONFIDENCE = 1.5f;

    private BoopLauncherSwipeGesture() { }

    static boolean isDeliberateLeftSwipe(
            float downX,
            float downY,
            float upX,
            float upY,
            float density) {
        float horizontalDistance = downX - upX;
        float verticalDistance = Math.abs(upY - downY);
        float requiredDistance = MIN_HORIZONTAL_DISTANCE_DP * density;
        return horizontalDistance >= requiredDistance
                && horizontalDistance >= verticalDistance * DIRECTIONAL_CONFIDENCE;
    }
}

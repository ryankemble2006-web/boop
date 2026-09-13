package com.boop.eyes;

/** BOOP-owned motion policy, deliberately independent of Android animator scale. */
public final class MotionPolicy {
    private MotionPolicy() { }

    public static boolean shouldReduce(boolean powerSaveMode, float androidAnimatorScale) {
        return powerSaveMode;
    }

    public static double scaledDelta(double elapsedMs, double speedMultiplier) {
        if (!Double.isFinite(elapsedMs) || elapsedMs <= 0) return 0;
        if (!Double.isFinite(speedMultiplier) || speedMultiplier <= 0) return 0;
        return elapsedMs * speedMultiplier;
    }
}

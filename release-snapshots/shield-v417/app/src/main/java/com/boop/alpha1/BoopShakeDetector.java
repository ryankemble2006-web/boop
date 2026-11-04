package com.boop.alpha1;

final class BoopShakeDetector {
    private static final float GRAVITY_EARTH = 9.80665f;
    private static final float GRAVITY_FILTER_ALPHA = 0.92f;
    private static final float PEAK_THRESHOLD_G = 1.25f;
    private static final float FULL_STRENGTH_G = 3.5f;
    private static final int REQUIRED_PEAKS = 3;
    private static final long MIN_PEAK_GAP_MS = 70L;
    private static final long PEAK_WINDOW_MS = 600L;
    private static final long COOLDOWN_MS = 1_500L;

    private boolean gravityReady;
    private float gravityX;
    private float gravityY;
    private float gravityZ;
    private int peakCount;
    private long firstPeakMs = -1L;
    private long lastPeakMs = -1L;
    private long cooldownUntilMs = Long.MIN_VALUE;
    private float burstMaxG;
    private float lastTriggerStrength;

    boolean onAccelerometer(float x, float y, float z, long timestampMs) {
        if (!gravityReady) {
            gravityReady = true;
            gravityX = x;
            gravityY = y;
            gravityZ = z;
            return false;
        }

        gravityX = GRAVITY_FILTER_ALPHA * gravityX + (1f - GRAVITY_FILTER_ALPHA) * x;
        gravityY = GRAVITY_FILTER_ALPHA * gravityY + (1f - GRAVITY_FILTER_ALPHA) * y;
        gravityZ = GRAVITY_FILTER_ALPHA * gravityZ + (1f - GRAVITY_FILTER_ALPHA) * z;

        float linearX = x - gravityX;
        float linearY = y - gravityY;
        float linearZ = z - gravityZ;
        float linearG = (float) Math.sqrt(
                linearX * linearX + linearY * linearY + linearZ * linearZ) / GRAVITY_EARTH;

        if (timestampMs < cooldownUntilMs || linearG < PEAK_THRESHOLD_G) {
            return false;
        }
        if (lastPeakMs >= 0L && timestampMs - lastPeakMs < MIN_PEAK_GAP_MS) {
            return false;
        }
        if (firstPeakMs < 0L || timestampMs - firstPeakMs > PEAK_WINDOW_MS) {
            peakCount = 0;
            firstPeakMs = timestampMs;
            burstMaxG = 0f;
        }

        peakCount++;
        lastPeakMs = timestampMs;
        burstMaxG = Math.max(burstMaxG, linearG);
        if (peakCount < REQUIRED_PEAKS) {
            return false;
        }

        float span = Math.max(0.01f, FULL_STRENGTH_G - PEAK_THRESHOLD_G);
        lastTriggerStrength = clamp01((burstMaxG - PEAK_THRESHOLD_G) / span);
        cooldownUntilMs = timestampMs + COOLDOWN_MS;
        peakCount = 0;
        firstPeakMs = -1L;
        lastPeakMs = -1L;
        burstMaxG = 0f;
        return true;
    }

    float lastTriggerStrength() {
        return lastTriggerStrength;
    }

    void reset() {
        gravityReady = false;
        gravityX = 0f;
        gravityY = 0f;
        gravityZ = 0f;
        peakCount = 0;
        firstPeakMs = -1L;
        lastPeakMs = -1L;
        cooldownUntilMs = Long.MIN_VALUE;
        burstMaxG = 0f;
        lastTriggerStrength = 0f;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}

package com.boop.alpha1;

/** Pure sleep eyelid timing. Reuses the accepted idle blink as the lead-in. */
final class BoopSleepCharm {
    static final long BLINK_DURATION_MS = BoopIdleBlink.DURATION_MS;
    static final long OPEN_SETTLE_MS = 100L;
    static final long DROWSY_DROOP_MS = 320L;
    static final long HALF_LID_PAUSE_MS = 120L;
    static final long FINAL_CLOSE_MS = 520L;
    static final long TOTAL_DURATION_MS = BLINK_DURATION_MS + OPEN_SETTLE_MS
            + DROWSY_DROOP_MS + HALF_LID_PAUSE_MS + FINAL_CLOSE_MS;

    private static final float HALF_LID_OPENNESS = 0.52f;
    private static final float CLOSED_OPENNESS = 0.04f;

    private BoopSleepCharm() { }

    static float openness(float progress) {
        float p = clamp(progress);
        long elapsed = Math.round(p * TOTAL_DURATION_MS);

        if (elapsed <= BLINK_DURATION_MS) {
            return BoopIdleBlink.openness(elapsed / (float) BLINK_DURATION_MS);
        }

        elapsed -= BLINK_DURATION_MS;
        if (elapsed <= OPEN_SETTLE_MS) {
            return 1f;
        }

        elapsed -= OPEN_SETTLE_MS;
        if (elapsed <= DROWSY_DROOP_MS) {
            return lerp(1f, HALF_LID_OPENNESS, smooth(elapsed / (float) DROWSY_DROOP_MS));
        }

        elapsed -= DROWSY_DROOP_MS;
        if (elapsed <= HALF_LID_PAUSE_MS) {
            return HALF_LID_OPENNESS;
        }

        elapsed -= HALF_LID_PAUSE_MS;
        return lerp(HALF_LID_OPENNESS, CLOSED_OPENNESS,
                smooth(elapsed / (float) FINAL_CLOSE_MS));
    }

    static float alpha(float progress) {
        float p = clamp(progress);
        float fadeStart = 0.92f;
        if (p <= fadeStart) return 1f;
        return 1f - smooth((p - fadeStart) / (1f - fadeStart));
    }

    private static float lerp(float start, float end, float amount) {
        return start + (end - start) * amount;
    }

    private static float smooth(float value) {
        float t = clamp(value);
        return t * t * (3f - 2f * t);
    }

    private static float clamp(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, Math.min(1f, value));
    }
}

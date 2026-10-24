package com.boop.alpha1;

import java.util.Objects;
import java.util.Random;

/** Pure timing/eyelid geometry; never changes presence or the sleep deadline. */
final class BoopIdleBlink {
    static final long DURATION_MS = 183L;
    static final long DOUBLE_GAP_MS = 110L;
    private static final int DOUBLE_BLINK_PERCENT = 18;

    private BoopIdleBlink() { }

    static long nextDelayMillis(Random random) {
        return 3_000L + Objects.requireNonNull(random, "random").nextInt(4_001);
    }

    static boolean shouldDoubleBlink(Random random) {
        return Objects.requireNonNull(random, "random").nextInt(100) < DOUBLE_BLINK_PERCENT;
    }

    static float openness(float progress) {
        if (!Float.isFinite(progress) || progress <= 0f || progress >= 1f) return 1f;
        float elapsed = progress * DURATION_MS;
        if (elapsed < 73.2f) return 1f - smooth(elapsed / 73.2f);
        if (elapsed <= 81.2f) return 0f;
        return smooth((elapsed - 81.2f) / 101.8f);
    }

    private static float smooth(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }
}

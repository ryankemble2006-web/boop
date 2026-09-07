package com.boop.alpha1;

import java.util.Objects;
import java.util.Random;

/** Pure timing/eyelid geometry; never changes presence or the sleep deadline. */
final class BoopIdleBlink {
    static final long DURATION_MS = 183L;
    private BoopIdleBlink() { }

    static long nextDelayMillis(Random random) {
        return 3_000L + Objects.requireNonNull(random, "random").nextInt(4_001);
    }

    static float openness(float progress) {
        if (!Float.isFinite(progress) || progress <= 0f || progress >= 1f) return 1f;
        // Close slightly faster than reopening; leave a thin, soft eyelid line.
        if (progress <= 0.4f) return 1f - 0.95f * smooth(progress / 0.4f);
        return 0.05f + 0.95f * smooth((progress - 0.4f) / 0.6f);
    }

    private static float smooth(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }
}

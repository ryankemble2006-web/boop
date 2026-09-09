package com.boop.shieldhome;

import java.util.Objects;
import java.util.Random;

/** Pure natural-blink timing shared by the Now Playing puppet renderer. */
final class NowPlayingPuppetBlink {
    static final long DURATION_MS = 183L;
    static final long DOUBLE_GAP_MS = 110L;
    private static final int DOUBLE_BLINK_PERCENT = 18;

    private NowPlayingPuppetBlink() { }

    static long nextDelayMillis(Random random) {
        return 3_000L + Objects.requireNonNull(random, "random").nextInt(4_001);
    }

    static boolean shouldDoubleBlink(Random random) {
        return Objects.requireNonNull(random, "random").nextInt(100) < DOUBLE_BLINK_PERCENT;
    }

    static float openness(float progress) {
        if (!Float.isFinite(progress) || progress <= 0f || progress >= 1f) return 1f;
        // Reuse BOOP's proven cadence: close slightly faster than reopening,
        // leaving a thin soft slit at full closure rather than a hard snap.
        if (progress <= 0.4f) return 1f - 0.95f * smooth(progress / 0.4f);
        return 0.05f + 0.95f * smooth((progress - 0.4f) / 0.6f);
    }

    private static float smooth(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }
}

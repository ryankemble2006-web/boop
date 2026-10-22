package com.boop.shieldhome;

import java.util.Objects;
import java.util.Random;

/** Pure natural-blink timing shared by the Now Playing puppet renderer. */
final class NowPlayingPuppetBlink {
    static final long DURATION_MS = 183L;
    static final long DOUBLE_GAP_MS = 110L;
    private static final int DOUBLE_BLINK_PERCENT = 18;
    private static final float CLOSE_MS = 73.2f;
    private static final float HOLD_END_MS = 81.2f;
    private static final float REOPEN_MS = 101.8f;

    private NowPlayingPuppetBlink() { }

    static long nextDelayMillis(Random random) {
        return 3_000L + Objects.requireNonNull(random, "random").nextInt(4_001);
    }

    static boolean shouldDoubleBlink(Random random) {
        return Objects.requireNonNull(random, "random").nextInt(100) < DOUBLE_BLINK_PERCENT;
    }

    static float openness(float progress) {
        if (!Float.isFinite(progress) || progress <= 0f || progress >= 1f) return 1f;
        float elapsed = progress * DURATION_MS;
        float closure;
        if (elapsed < CLOSE_MS) closure = smooth(elapsed / CLOSE_MS);
        else if (elapsed <= HOLD_END_MS) closure = 1f;
        else closure = 1f - smooth((elapsed - HOLD_END_MS) / REOPEN_MS);
        return 1f - 0.95f * closure;
    }

    private static float smooth(float value) {
        float t = Math.max(0f, Math.min(1f, value));
        return t * t * (3f - 2f * t);
    }
}

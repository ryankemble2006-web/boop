package com.boop.shieldhome;

/** Short measured-onset hops, not a free-running BPM clock or continuous VU height. */
final class MusicBeatPulse {
    private long previousSampleMs = -1L, hitMs = -1L;
    private float previousLevel, strength;

    float update(float level, long sampleMs, long nowMs) {
        if (!Float.isFinite(level) || level < 0f || level > 1f
                || sampleMs < 0L || nowMs < sampleMs || nowMs - sampleMs > 350L) {
            reset();
            return 0f;
        }
        if (sampleMs > previousSampleMs) {
            if (previousSampleMs >= 0L && sampleMs - previousSampleMs <= 1000L) {
                float rise = level - previousLevel;
                float threshold = Math.max(0.035f, previousLevel * 0.18f);
                if (rise >= threshold && (hitMs < 0L || nowMs - hitMs >= 240L)) {
                    hitMs = nowMs;
                    strength = Math.min(1f, 0.45f + rise * 3f);
                }
            }
            previousLevel = level;
            previousSampleMs = sampleMs;
        }
        if (hitMs < 0L || nowMs < hitMs) return 0f;
        float remaining = Math.max(0f, 1f - (nowMs - hitMs) / 220f);
        return strength * remaining * remaining;
    }

    void reset() {
        previousSampleMs = -1L;
        hitMs = -1L;
        previousLevel = 0f;
        strength = 0f;
    }
}

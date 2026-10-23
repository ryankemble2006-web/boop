package com.boop.shieldhome;

/** Pure launcher puppetry derived from the already-proven BOOP headphones motion. */
final class NowPlayingPuppetMotion {
    static final long GROOVE_PERIOD_MS = 2400L;
    static final long UPSET_PERIOD_MS = 5200L;
    static final long ACK_DURATION_MS = 700L;
    private static final double TWO_PI = Math.PI * 2.0;

    static final class Pose {
        final float x;
        final float y;
        final float rotationDegrees;
        final float scale;

        Pose(float x, float y, float rotationDegrees, float scale) {
            this.x = x;
            this.y = y;
            this.rotationDegrees = rotationDegrees;
            this.scale = scale;
        }
    }

    private NowPlayingPuppetMotion() { }

    static Pose rest() {
        return new Pose(0f, 0f, 0f, 1f);
    }

    /** Continuous playback dance: side-to-side lean, double bounce and gentle squash/stretch. */
    static Pose groove(long elapsedMs) {
        long normalized = Math.floorMod(elapsedMs, GROOVE_PERIOD_MS);
        double phase = TWO_PI * normalized / GROOVE_PERIOD_MS;
        double sway = Math.sin(phase);
        double counterSway = Math.sin(phase * 2.0 + 0.55);
        double doubleBounce = Math.abs(Math.sin(phase * 2.0));
        double kick = Math.sin(phase * 4.0);
        return new Pose(
                (float) (7.5 * sway + 2.2 * counterSway),
                (float) (-3.0 - 10.5 * doubleBounce - 1.8 * kick * kick),
                (float) (4.2 * sway + 1.1 * counterSway),
                (float) (1.0 + 0.035 * Math.sin(phase * 2.0)));
    }

    /** Paused-media attitude: a small sulk with a recurring irritated DJ head-shake. */
    static Pose upset(long elapsedMs) {
        long normalized = Math.floorMod(elapsedMs, UPSET_PERIOD_MS);
        double slowPhase = TWO_PI * normalized / UPSET_PERIOD_MS;
        double shake = upsetShake(normalized, 1150L, 1000L, 3.0)
                + 0.65 * upsetShake(normalized, 3550L, 800L, 2.0);
        return new Pose(
                (float) (3.5 + 1.4 * shake),
                (float) (5.8 + 0.7 * Math.cos(slowPhase)),
                (float) (-4.5 + 3.8 * shake),
                (float) (0.985 - 0.006 * Math.abs(shake)));
    }

    /** Brief perk layered over the current dance/upset/rest pose after a track or session change. */
    static Pose acknowledge(Pose base, long elapsedMs) {
        if (base == null) base = rest();
        if (elapsedMs < 0L || elapsedMs >= ACK_DURATION_MS) return base;
        float progress = clamp(elapsedMs / (float) ACK_DURATION_MS);
        float envelope = (float) Math.sin(Math.PI * progress);
        return new Pose(
                base.x + 4f * envelope,
                base.y - 22f * envelope,
                base.rotationDegrees + 3f * envelope,
                base.scale * (1f + 0.025f * envelope));
    }

    private static double upsetShake(long elapsedMs, long startMs, long durationMs, double cycles) {
        if (elapsedMs < startMs || elapsedMs >= startMs + durationMs) return 0.0;
        double progress = (elapsedMs - startMs) / (double) durationMs;
        double envelope = Math.sin(Math.PI * progress);
        return Math.sin(TWO_PI * cycles * progress) * envelope;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}

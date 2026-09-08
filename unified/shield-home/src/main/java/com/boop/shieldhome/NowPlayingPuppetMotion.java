package com.boop.shieldhome;

/** Pure launcher puppetry derived from the already-proven BOOP headphones motion. */
final class NowPlayingPuppetMotion {
    static final long GROOVE_PERIOD_MS = 3600L;
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

    static Pose groove(long elapsedMs) {
        long normalized = Math.floorMod(elapsedMs, GROOVE_PERIOD_MS);
        double phase = TWO_PI * normalized / GROOVE_PERIOD_MS;
        double sway = Math.sin(phase);
        double doublePhase = Math.sin(phase * 2.0);
        double characterSway = Math.sin(phase * 2.0 + 0.65);
        double listeningWeight = Math.sin(phase + 0.8);
        double headLag = Math.sin(phase * 2.0 + 1.0);
        return new Pose(
                (float) (6.0 * sway + 2.2 * characterSway),
                (float) (-9.0 * doublePhase * doublePhase - 2.2 - 2.0 * listeningWeight),
                (float) (1.8 * sway + 0.65 * headLag),
                1f);
    }

    /** Brief perk layered over the current rest/groove pose after a track or session change. */
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

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}

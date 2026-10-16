package com.boop.shieldoverlay;

public final class MediaPuppetMotion {
    public static final long MUSIC_PERIOD_MS = 3600L;
    public static final long CINEMA_PERIOD_MS = 12000L;

    private static final double TWO_PI = Math.PI * 2.0;

    private static final long[] CINEMA_TIMES_MS = {
        0L, 4400L, 5900L, 6500L, 6700L, 7200L, 8900L, 9300L, 9450L, 12000L
    };

    private static final Pose[] CINEMA_POSES = {
        new Pose(0f, 0f, 0f, 1f),
        new Pose(0f, 0f, 0f, 1f),
        new Pose(105f, -185f, 9f, 1f),
        new Pose(105f, -185f, 9f, 1f),
        new Pose(105f, -185f, 9f, 0f),
        new Pose(105f, -185f, 9f, 0f),
        new Pose(0f, 0f, 0f, 0f),
        new Pose(0f, 0f, 0f, 0f),
        new Pose(0f, 0f, 0f, 1f),
        new Pose(0f, 0f, 0f, 1f)
    };

    private MediaPuppetMotion() {}

    /** Returns offsets for the whole character during music playback. */
    public static Pose music(long elapsedMs) {
        long normalizedMs = Math.floorMod(elapsedMs, MUSIC_PERIOD_MS);
        double phase = TWO_PI * normalizedMs / MUSIC_PERIOD_MS;
        double sway = Math.sin(phase);
        double bounce = Math.sin(phase * 2.0);

        return new Pose(
                (float) (6.0 * sway),
                (float) (-9.0 * bounce * bounce),
                (float) (1.8 * sway),
                1f);
    }

    /** Returns offsets for the character's hand during cinema playback. */
    public static Pose cinema(long elapsedMs) {
        long normalizedMs = Math.floorMod(elapsedMs, CINEMA_PERIOD_MS);
        for (int index = 1; index < CINEMA_TIMES_MS.length; index++) {
            if (normalizedMs <= CINEMA_TIMES_MS[index]) {
                long startMs = CINEMA_TIMES_MS[index - 1];
                long endMs = CINEMA_TIMES_MS[index];
                float progress = (float) (normalizedMs - startMs) / (endMs - startMs);
                return interpolate(
                        CINEMA_POSES[index - 1], CINEMA_POSES[index], smoothstep(progress));
            }
        }
        throw new AssertionError("normalized cinema time outside keyframes");
    }

    private static float smoothstep(float progress) {
        return progress * progress * (3f - 2f * progress);
    }

    private static Pose interpolate(Pose start, Pose end, float progress) {
        return new Pose(
                lerp(start.x, end.x, progress),
                lerp(start.y, end.y, progress),
                lerp(start.rotationDegrees, end.rotationDegrees, progress),
                lerp(start.kernelAlpha, end.kernelAlpha, progress));
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    public static final class Pose {
        public final float x;
        public final float y;
        public final float rotationDegrees;
        public final float kernelAlpha;

        public Pose(float x, float y, float rotationDegrees, float kernelAlpha) {
            this.x = x;
            this.y = y;
            this.rotationDegrees = rotationDegrees;
            this.kernelAlpha = kernelAlpha;
        }
    }
}

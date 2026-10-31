package com.boop.shieldoverlay;

/** Pure fullscreen Deezer puppetry. Keeps playback detection and timing ownership outside rendering. */
final class FullscreenPuppetMotion {
    static final long SETTLE_DURATION_MS = 520L;
    static final long TRACK_CHANGE_DURATION_MS = 700L;
    private static final long TRACK_PERK_MS = 180L;
    private static final double TWO_PI = Math.PI * 2.0;

    private FullscreenPuppetMotion() { }

    static MediaPuppetMotion.Pose groove(long elapsedMs) {
        long normalized = Math.floorMod(elapsedMs, MediaPuppetMotion.MUSIC_PERIOD_MS);
        double phase = TWO_PI * normalized / MediaPuppetMotion.MUSIC_PERIOD_MS;
        double baseSway = Math.sin(phase);
        double doublePhase = Math.sin(phase * 2.0);
        double characterSway = Math.sin(phase * 2.0 + 0.65);
        double listeningWeight = Math.sin(phase + 0.8);
        double headLag = Math.sin(phase * 2.0 + 1.0);

        float x = (float) (6.0 * baseSway + 2.2 * characterSway);
        float y = (float) (-9.0 * doublePhase * doublePhase - 2.2 - 2.0 * listeningWeight);
        float rotation = (float) (1.8 * baseSway + 0.65 * headLag);
        return new MediaPuppetMotion.Pose(x, y, rotation, 1f);
    }

    static MediaPuppetMotion.Pose rest() {
        return new MediaPuppetMotion.Pose(0f, 0f, 0f, 1f);
    }

    static MediaPuppetMotion.Pose settle(MediaPuppetMotion.Pose start, long elapsedMs) {
        if (start == null) {
            start = rest();
        }
        float progress = clamp(elapsedMs / (float) SETTLE_DURATION_MS);
        float remaining = 1f - smooth(progress);
        return new MediaPuppetMotion.Pose(
                start.x * remaining,
                start.y * remaining,
                start.rotationDegrees * remaining,
                1f);
    }

    static MediaPuppetMotion.Pose trackChange(long elapsedMs) {
        if (elapsedMs <= 0L || elapsedMs >= TRACK_CHANGE_DURATION_MS) {
            return rest();
        }
        MediaPuppetMotion.Pose perk = new MediaPuppetMotion.Pose(4f, -26f, 3.2f, 1f);
        if (elapsedMs <= TRACK_PERK_MS) {
            float p = smooth(clamp(elapsedMs / (float) TRACK_PERK_MS));
            return interpolate(rest(), perk, p);
        }
        float p = smooth(clamp((elapsedMs - TRACK_PERK_MS)
                / (float) (TRACK_CHANGE_DURATION_MS - TRACK_PERK_MS)));
        return interpolate(perk, rest(), p);
    }

    private static MediaPuppetMotion.Pose interpolate(
            MediaPuppetMotion.Pose start, MediaPuppetMotion.Pose end, float progress) {
        return new MediaPuppetMotion.Pose(
                lerp(start.x, end.x, progress),
                lerp(start.y, end.y, progress),
                lerp(start.rotationDegrees, end.rotationDegrees, progress),
                1f);
    }

    private static float smooth(float value) {
        return value * value * (3f - 2f * value);
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }
}

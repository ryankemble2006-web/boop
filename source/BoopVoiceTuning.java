package com.boop.alpha1;

final class BoopVoiceTuning {
    static final int PROGRESS_MAX = 1000;

    static final float MIN_PITCH = 0.75f;
    static final float MAX_PITCH = 1.45f;
    static final float DEFAULT_PITCH = 1.12f;

    static final float MIN_RATE = 0.70f;
    static final float MAX_RATE = 1.25f;
    static final float DEFAULT_RATE = 0.96f;

    private BoopVoiceTuning() { }

    static float pitchFromProgress(int progress) {
        return fromProgress(progress, MIN_PITCH, MAX_PITCH);
    }

    static float rateFromProgress(int progress) {
        return fromProgress(progress, MIN_RATE, MAX_RATE);
    }

    static int progressFromPitch(float pitch) {
        return toProgress(pitch, MIN_PITCH, MAX_PITCH);
    }

    static int progressFromRate(float rate) {
        return toProgress(rate, MIN_RATE, MAX_RATE);
    }

    static float clampPitch(float pitch) {
        return clamp(pitch, MIN_PITCH, MAX_PITCH);
    }

    static float clampRate(float rate) {
        return clamp(rate, MIN_RATE, MAX_RATE);
    }

    private static float fromProgress(int progress, float min, float max) {
        int bounded = Math.max(0, Math.min(PROGRESS_MAX, progress));
        float fraction = bounded / (float) PROGRESS_MAX;
        return min + ((max - min) * fraction);
    }

    private static int toProgress(float value, float min, float max) {
        float bounded = clamp(value, min, max);
        float fraction = (bounded - min) / (max - min);
        return Math.round(fraction * PROGRESS_MAX);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

package com.boop.alpha1;

final class BoopVoiceTuning {
    static final int PROGRESS_MAX = 1000;

    static final float MIN_PITCH = 0.5f;
    static final float MAX_PITCH = 2.0f;
    static final float DEFAULT_PITCH = 1.0f;

    static final float MIN_RATE = 0.5f;
    static final float MAX_RATE = 2.0f;
    static final float DEFAULT_RATE = 1.0f;

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
        return fraction<=0.5f ? min+(1f-min)*fraction*2f : 1f+(max-1f)*(fraction-0.5f)*2f;
    }

    private static int toProgress(float value, float min, float max) {
        float bounded = clamp(value, min, max);
        float fraction = bounded<=1f ? (bounded-min)/(1f-min)*0.5f : 0.5f+(bounded-1f)/(max-1f)*0.5f;
        return Math.round(fraction * PROGRESS_MAX);
    }

    private static float clamp(float value, float min, float max) {
        return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : 1f;
    }
}

package com.boop.alpha1;

final class BoopEyeHueMath {
    static final int PROGRESS_MAX = 359;
    static final int DEFAULT_HUE_DEGREES = 190;

    private BoopEyeHueMath() { }

    static int clampHue(int hueDegrees) {
        return Math.max(0, Math.min(PROGRESS_MAX, hueDegrees));
    }

    static float rotationDegreesForHue(int hueDegrees) {
        return clampHue(hueDegrees) - DEFAULT_HUE_DEGREES;
    }

    static float[] matrixForHue(int hueDegrees) {
        int bounded = clampHue(hueDegrees);
        if (bounded == DEFAULT_HUE_DEGREES) {
            return null;
        }

        double radians = Math.toRadians(rotationDegreesForHue(bounded));
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);

        return new float[]{
                0.213f + cosine * 0.787f - sine * 0.213f,
                0.715f - cosine * 0.715f - sine * 0.715f,
                0.072f - cosine * 0.072f + sine * 0.928f,
                0f,
                0f,

                0.213f - cosine * 0.213f + sine * 0.143f,
                0.715f + cosine * 0.285f + sine * 0.140f,
                0.072f - cosine * 0.072f - sine * 0.283f,
                0f,
                0f,

                0.213f - cosine * 0.213f - sine * 0.787f,
                0.715f - cosine * 0.715f + sine * 0.715f,
                0.072f + cosine * 0.928f + sine * 0.072f,
                0f,
                0f,

                0f,
                0f,
                0f,
                1f,
                0f
        };
    }
}

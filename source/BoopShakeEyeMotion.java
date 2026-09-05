package com.boop.alpha1;

final class BoopShakeEyeMotion {
    private static final float[] TIMES = {0f, 0.17f, 0.34f, 0.52f, 0.70f, 0.84f, 1f};
    private static final float[] LEFT_X = {0f, 1f, -1f, 0.65f, -0.45f, 0.15f, 0f};
    private static final float[] LEFT_Y = {0f, -1f, 1f, -0.70f, 0.45f, -0.15f, 0f};
    private static final float[] RIGHT_X = {0f, -1f, 1f, -0.55f, 0.40f, -0.12f, 0f};
    private static final float[] RIGHT_Y = {0f, 1f, -1f, 0.65f, -0.35f, 0.12f, 0f};

    private BoopShakeEyeMotion() { }

    static Pose pose(
            boolean leftEye,
            float fraction,
            float viewportWidth,
            float viewportHeight,
            float baseCenterX,
            float baseCenterY,
            float eyeWidth,
            float eyeHeight,
            float strength) {
        float t = clamp01(fraction);
        if (t >= 1f || viewportWidth <= 0f || viewportHeight <= 0f) {
            return new Pose(baseCenterX, baseCenterY, 1f, 1f, 0f);
        }

        float s = clamp01(strength);
        float decay = 1f - t;
        float wobble = (float) Math.sin(t * Math.PI * 8.0) * decay * (0.055f + 0.045f * s);
        float scaleX = Math.max(0.88f, 1f + wobble);
        float scaleY = Math.max(0.88f, 1f - wobble * 0.78f);
        float rotation = (float) Math.sin(t * Math.PI * 7.0)
                * decay * (4.5f + 5.5f * s) * (leftEye ? 1f : -1f);

        float codeX = interpolate(leftEye ? LEFT_X : RIGHT_X, t);
        float codeY = interpolate(leftEye ? LEFT_Y : RIGHT_Y, t);
        float reach = 0.72f + 0.28f * s;

        float halfWidth = Math.min(viewportWidth / 2f, eyeWidth * scaleX / 2f);
        float halfHeight = Math.min(viewportHeight / 2f, eyeHeight * scaleY / 2f);
        float minCenterX = halfWidth;
        float maxCenterX = viewportWidth - halfWidth;
        float minCenterY = halfHeight;
        float maxCenterY = viewportHeight - halfHeight;

        float centerX = targetFromCode(baseCenterX, minCenterX, maxCenterX, codeX, reach);
        float centerY = targetFromCode(baseCenterY, minCenterY, maxCenterY, codeY, reach);
        centerX = clamp(centerX, minCenterX, maxCenterX);
        centerY = clamp(centerY, minCenterY, maxCenterY);

        return new Pose(centerX, centerY, scaleX, scaleY, rotation);
    }

    private static float interpolate(float[] values, float t) {
        for (int i = 0; i < TIMES.length - 1; i++) {
            if (t <= TIMES[i + 1]) {
                float span = TIMES[i + 1] - TIMES[i];
                float local = span <= 0f ? 0f : (t - TIMES[i]) / span;
                local = smoothStep(clamp01(local));
                return values[i] + (values[i + 1] - values[i]) * local;
            }
        }
        return values[values.length - 1];
    }

    private static float targetFromCode(
            float base,
            float minimum,
            float maximum,
            float code,
            float reach) {
        if (code < 0f) {
            return base + (minimum - base) * Math.min(1f, -code * reach);
        }
        if (code > 0f) {
            return base + (maximum - base) * Math.min(1f, code * reach);
        }
        return base;
    }

    private static float smoothStep(float value) {
        return value * value * (3f - 2f * value);
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }

    static final class Pose {
        private final float centerX;
        private final float centerY;
        private final float scaleX;
        private final float scaleY;
        private final float rotation;

        Pose(float centerX, float centerY, float scaleX, float scaleY, float rotation) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.rotation = rotation;
        }

        float centerX() { return centerX; }
        float centerY() { return centerY; }
        float scaleX() { return scaleX; }
        float scaleY() { return scaleY; }
        float rotation() { return rotation; }
    }
}

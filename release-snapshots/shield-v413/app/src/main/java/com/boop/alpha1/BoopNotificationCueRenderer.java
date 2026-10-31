package com.boop.alpha1;

final class BoopNotificationCueRenderer {
    private static final double DURATION_SECONDS = 0.320;
    private static final double MAX_AMPLITUDE = 0.72;

    private BoopNotificationCueRenderer() { }

    static short[] render(int sampleRate) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("sampleRate must be positive");
        }
        int sampleCount = (int) Math.round(sampleRate * DURATION_SECONDS);
        short[] pcm = new short[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            double t = i / (double) sampleRate;
            double knock1 = t < 0.050
                    ? Math.sin(2.0 * Math.PI * 190.0 * t)
                            * Math.exp(-55.0 * t) * 0.42
                    : 0.0;

            double u2 = t - 0.078;
            double knock2 = u2 >= 0.0 && u2 < 0.050
                    ? Math.sin(2.0 * Math.PI * 285.0 * u2)
                            * Math.exp(-52.0 * u2) * 0.34
                    : 0.0;

            double uc = t - 0.138;
            double chirp = 0.0;
            if (uc >= 0.0 && uc < 0.170) {
                double phase = 2.0 * Math.PI
                        * (520.0 * uc + 0.5 * 1650.0 * uc * uc);
                double envelope = Math.sin(Math.PI * uc / 0.170);
                chirp = Math.sin(phase) * envelope * 0.24;
            }

            double value = clamp(knock1 + knock2 + chirp, -MAX_AMPLITUDE, MAX_AMPLITUDE);
            pcm[i] = (short) Math.round(value * Short.MAX_VALUE);
        }
        return pcm;
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}

package com.boop.shieldhome;

/** Deterministic tone steps: measures signal delay, not host execution time. */
public final class SpectrumTimingCheck {
    static final int RATE = 44100, BLOCK = 256;
    static short[] tone(double hz, long first, boolean silent) {
        short[] pcm = new short[BLOCK * 2];
        if (!silent) for (int i = 0; i < BLOCK; i++) {
            short sample = (short) (16384 * Math.sin(2 * Math.PI * hz * (first + i) / RATE));
            pcm[i * 2] = sample; pcm[i * 2 + 1] = sample;
        }
        return pcm;
    }
    static double[] response(double hz) {
        PcmSpectrum reference = new PcmSpectrum(RATE);
        float[] steady = null;
        for (int n = 0; n < RATE * 2; n += BLOCK) steady = reference.feed(tone(hz, n, false), BLOCK * 2);
        int band = 0;
        for (int i = 1; i < steady.length; i++) if (steady[i] > steady[band]) band = i;
        PcmSpectrum subject = new PcmSpectrum(RATE);
        for (int n = 0; n < RATE; n += BLOCK) subject.feed(tone(hz, n, true), BLOCK * 2);
        double rise = -1, fall = -1;
        for (int n = 0; n < RATE * 2; n += BLOCK) {
            float value = subject.feed(tone(hz, n, false), BLOCK * 2)[band];
            if (rise < 0 && value >= steady[band] - 3f / 72) rise = (n + BLOCK) * 1000.0 / RATE;
        }
        for (int n = 0; n < RATE; n += BLOCK) {
            float value = subject.feed(tone(hz, n, true), BLOCK * 2)[band];
            if (fall < 0 && value <= steady[band] - 20f / 72) fall = (n + BLOCK) * 1000.0 / RATE;
        }
        return new double[]{rise, fall};
    }
    public static void main(String[] args) {
        boolean measureOnly = args.length > 0;
        for (double hz : new double[]{20, 40, 80, 250, 1000, 8000}) {
            double[] response = response(hz);
            System.out.printf(java.util.Locale.US, "%.0f Hz: attack(-3dB)=%.1fms, release(-20dB)=%.1fms%n", hz, response[0], response[1]);
            double limit = hz <= 20 ? 280 : hz <= 40 ? 160 : hz <= 80 ? 100 : 45;
            if (!measureOnly && (response[0] < 0 || response[0] > limit))
                throw new AssertionError(hz + " Hz attack exceeds " + limit + "ms");
            double releaseLimit = hz <= 20 ? 500 : hz <= 40 ? 280 : hz <= 80 ? 160 : 65;
            if (!measureOnly && (response[1] < 0 || response[1] > releaseLimit))
                throw new AssertionError(hz + " Hz release exceeds " + releaseLimit + "ms");
        }
    }
}

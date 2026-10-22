package com.boop.shieldhome;

/** Continuous stereo band energy, 20 Hz–16 kHz. Fixed dBFS scale; no AGC or synthetic motion. */
final class PcmSpectrum {
    static final int BANDS = 64;
    private final Band[] filters = new Band[BANDS];
    private final float[] latest = new float[BANDS];

    PcmSpectrum(int rate) {
        if (rate < 32000) throw new IllegalArgumentException("Spectrum needs at least 32 kHz PCM");
        for (int i = 0; i < BANDS; i++) filters[i] = new Band(rate, Math.sqrt(edge(i) * edge(i + 1)));
    }

    static double edge(int band) { return 20 * Math.pow(800, band / (double) BANDS); }

    float[] feed(short[] pcm, int count) {
        if (pcm == null || count < 2) return latest.clone();
        int end = Math.min(pcm.length, count) & ~1;
        for (int b = 0; b < BANDS; b++) {
            double power = filters[b].measure(pcm, end);
            double db = power > 0 ? 10 * Math.log10(power) : -120;
            latest[b] = (float) Math.max(0, Math.min(1, (db + 72) / 72));
        }
        return latest.clone();
    }

    private static final class Band {
        final double gain, a1, a2, smoothing;
        double l1, l2, l3, l4, r1, r2, r3, r4, power;

        Band(int rate, double hz) {
            // RBJ constant-0dB bandpass, with digital bandwidth warping compensation:
            // https://www.w3.org/TR/audio-eq-cookbook/#formulae
            // Two cascaded sections give steeper rejection. Broaden each section by
            // 1/sqrt(sqrt(2)-1), so their combined -3dB width matches one displayed band.
            double omega = 2 * Math.PI * hz / rate;
            double bandwidth = Math.log(800) / BANDS / Math.sqrt(Math.sqrt(2) - 1);
            double alpha = Math.sin(omega) * Math.sinh(bandwidth * .5 * omega / Math.sin(omega));
            gain = alpha / (1 + alpha);
            a1 = -2 * Math.cos(omega) / (1 + alpha);
            a2 = (1 - alpha) / (1 + alpha);
            // Average stereo power, not stereo samples: opposite-phase audio must survive.
            // Half a period smooths carrier ripple; high bands need only a 3ms envelope.
            smoothing = 1 - Math.exp(-1 / (rate * Math.max(.003, .5 / hz)));
        }

        double measure(short[] pcm, int end) {
            double ll1 = l1, ll2 = l2, ll3 = l3, ll4 = l4;
            double rr1 = r1, rr2 = r2, rr3 = r3, rr4 = r4, p = power;
            for (int i = 0; i < end; i += 2) {
                double left = pcm[i] / 32768.0, right = pcm[i + 1] / 32768.0;
                double yl = gain * left + ll1, yr = gain * right + rr1;
                ll1 = -a1 * yl + ll2; ll2 = -gain * left - a2 * yl;
                rr1 = -a1 * yr + rr2; rr2 = -gain * right - a2 * yr;
                double outL = gain * yl + ll3, outR = gain * yr + rr3;
                ll3 = -a1 * outL + ll4; ll4 = -gain * yl - a2 * outL;
                rr3 = -a1 * outR + rr4; rr4 = -gain * yr - a2 * outR;
                p += smoothing * ((outL * outL + outR * outR) * .5 - p);
            }
            l1 = ll1; l2 = ll2; l3 = ll3; l4 = ll4;
            r1 = rr1; r2 = rr2; r3 = rr3; r4 = rr4; power = p;
            return p;
        }
    }
}

package com.boop.alpha1;

/** Lightweight local pronunciation features. Amplitude is normalized and raw PCM is discarded. */
final class BoopWakePronunciationFeatures {
    private static final int TIME_BINS = 18;
    private static final double[] FREQUENCIES = {250, 375, 500, 750, 1000, 1500, 2250, 3250};

    static final class Result {
        private final float[] vector;
        private final float durationSeconds;
        Result(float[] vector, float durationSeconds) {
            this.vector = vector;
            this.durationSeconds = durationSeconds;
        }
        float[] vector() { return vector.clone(); }
        float durationSeconds() { return durationSeconds; }
    }

    private BoopWakePronunciationFeatures() { }

    static Result extract(short[] pcm, int sampleRate) {
        if (pcm == null || pcm.length < Math.max(160, sampleRate / 10) || sampleRate < 8000) return null;
        int frame = Math.max(80, sampleRate / 50); // 20 ms
        int frames = (pcm.length + frame - 1) / frame;
        double[] rms = new double[frames];
        double peak = 0d;
        for (int f = 0; f < frames; f++) {
            int from = f * frame;
            int to = Math.min(pcm.length, from + frame);
            double sum = 0d;
            for (int i = from; i < to; i++) {
                double value = pcm[i];
                sum += value * value;
            }
            rms[f] = Math.sqrt(sum / Math.max(1, to - from));
            peak = Math.max(peak, rms[f]);
        }
        if (peak < 120d) return null;
        double activeThreshold = Math.max(100d, peak * 0.16d);
        int first = -1, last = -1;
        for (int f = 0; f < frames; f++) {
            if (rms[f] >= activeThreshold) {
                if (first < 0) first = f;
                last = f;
            }
        }
        if (first < 0) return null;
        first = Math.max(0, first - 1);
        last = Math.min(frames - 1, last + 1);
        int start = first * frame;
        int end = Math.min(pcm.length, (last + 1) * frame);
        int activeSamples = end - start;
        float duration = activeSamples / (float) sampleRate;
        if (duration < 0.12f || duration > 2.6f) return null;

        double globalSquare = 0d;
        double mean = 0d;
        for (int i = start; i < end; i++) mean += pcm[i];
        mean /= Math.max(1, activeSamples);
        for (int i = start; i < end; i++) {
            double value = pcm[i] - mean;
            globalSquare += value * value;
        }
        double globalRms = Math.sqrt(globalSquare / Math.max(1, activeSamples));
        if (globalRms < 1d) return null;

        int featuresPerBin = 2 + FREQUENCIES.length;
        float[] vector = new float[TIME_BINS * featuresPerBin];
        for (int bin = 0; bin < TIME_BINS; bin++) {
            int from = start + (int) Math.floor(activeSamples * (bin / (double) TIME_BINS));
            int to = start + (int) Math.ceil(activeSamples * ((bin + 1d) / TIME_BINS));
            to = Math.min(end, Math.max(from + 1, to));
            double localSquare = 0d;
            int crossings = 0;
            double previous = pcm[from] - mean;
            for (int i = from; i < to; i++) {
                double value = pcm[i] - mean;
                localSquare += value * value;
                if (i > from && (value >= 0d) != (previous >= 0d)) crossings++;
                previous = value;
            }
            int count = Math.max(1, to - from);
            double localRms = Math.sqrt(localSquare / count);
            int offset = bin * featuresPerBin;
            vector[offset] = (float) Math.min(2.0d, localRms / globalRms);
            vector[offset + 1] = crossings / (float) count;

            double spectralTotal = 1e-9;
            double[] spectral = new double[FREQUENCIES.length];
            for (int k = 0; k < FREQUENCIES.length; k++) {
                double hz = Math.min(FREQUENCIES[k], sampleRate * 0.45d);
                double re = 0d, im = 0d;
                for (int i = from; i < to; i++) {
                    double window = count <= 1 ? 1d
                            : 0.5d - 0.5d * Math.cos(2d * Math.PI * (i - from) / (count - 1d));
                    double value = (pcm[i] - mean) * window;
                    double angle = 2d * Math.PI * hz * (i - from) / sampleRate;
                    re += value * Math.cos(angle);
                    im -= value * Math.sin(angle);
                }
                spectral[k] = Math.sqrt(re * re + im * im);
                spectralTotal += spectral[k];
            }
            for (int k = 0; k < spectral.length; k++) {
                vector[offset + 2 + k] = (float) (spectral[k] / spectralTotal);
            }
        }
        normalize(vector);
        return new Result(vector, duration);
    }

    private static void normalize(float[] values) {
        double sum = 0d;
        for (float value : values) sum += value * value;
        if (sum <= 1e-12) return;
        float scale = (float) (1d / Math.sqrt(sum));
        for (int i = 0; i < values.length; i++) values[i] *= scale;
    }
}

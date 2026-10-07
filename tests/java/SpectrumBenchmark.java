package com.boop.shieldhome;

/** Runs offline PCM through the analyser on a JVM or Android app_process; produces no audio. */
public final class SpectrumBenchmark {
    public static void main(String[] args) {
        PcmSpectrum spectrum = new PcmSpectrum(44100);
        short[] pcm = new short[512];
        for (int i = 0; i < pcm.length / 2; i++)
            pcm[2 * i] = pcm[2 * i + 1] = (short) (12000 * Math.sin(2 * Math.PI * 1000 * i / 44100));
        for (int i = 0; i < 1000; i++) spectrum.feed(pcm, pcm.length);
        long[] times = new long[1000]; long total = 0;
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime(); spectrum.feed(pcm, pcm.length);
            times[i] = System.nanoTime() - start; total += times[i];
        }
        java.util.Arrays.sort(times);
        System.out.printf(java.util.Locale.US,
                "256 frames (5.805ms budget): mean=%.3fms p95=%.3fms p99=%.3fms max=%.3fms; CPU/audio=%.1f%%%n",
                total / 1e6 / times.length, times[950] / 1e6, times[990] / 1e6,
                times[999] / 1e6, (total / 1e9) / (256.0 * times.length / 44100) * 100);
    }
}

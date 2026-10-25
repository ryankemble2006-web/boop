package com.boop.alpha1;

import java.util.Arrays;

/** Finds one spoken utterance from the existing wake microphone stream. */
final class BoopWakeUtteranceSegmenter {
    private final int sampleRate;
    private final int maxSamples;
    private short[] captured;
    private int capturedCount;
    private boolean active;
    private int trailingQuietChunks;
    private double noiseFloor = 120d;

    BoopWakeUtteranceSegmenter(int sampleRate) {
        if (sampleRate < 8000) throw new IllegalArgumentException("sampleRate too low");
        this.sampleRate = sampleRate;
        this.maxSamples = (int) (sampleRate * 2.6f);
        this.captured = new short[Math.max(sampleRate, 4096)];
    }

    short[] accept(short[] input, int count) {
        if (input == null || count <= 0) return null;
        int bounded = Math.min(count, input.length);
        double level = rms(input, bounded);
        double startThreshold = Math.max(180d, noiseFloor * 2.35d);
        double quietThreshold = Math.max(145d, noiseFloor * 1.55d);

        if (!active) {
            if (level < startThreshold) {
                noiseFloor = clamp(noiseFloor * 0.94d + level * 0.06d, 45d, 1200d);
                return null;
            }
            active = true;
            trailingQuietChunks = 0;
            capturedCount = 0;
        }

        append(input, bounded);
        if (level <= quietThreshold) trailingQuietChunks++;
        else trailingQuietChunks = 0;

        if (trailingQuietChunks >= 2 || capturedCount >= maxSamples) {
            short[] result = Arrays.copyOf(captured, capturedCount);
            resetAfterUtterance(level);
            return result;
        }
        return null;
    }

    void reset() {
        active = false;
        trailingQuietChunks = 0;
        capturedCount = 0;
        noiseFloor = 120d;
    }

    private void append(short[] input, int count) {
        int writable = Math.min(count, maxSamples - capturedCount);
        if (writable <= 0) return;
        int needed = capturedCount + writable;
        if (needed > captured.length) {
            int grown = Math.min(maxSamples, Math.max(needed, captured.length * 2));
            captured = Arrays.copyOf(captured, grown);
        }
        System.arraycopy(input, 0, captured, capturedCount, writable);
        capturedCount += writable;
    }

    private void resetAfterUtterance(double finalLevel) {
        active = false;
        trailingQuietChunks = 0;
        capturedCount = 0;
        if (finalLevel < noiseFloor * 2d) {
            noiseFloor = clamp(noiseFloor * 0.9d + finalLevel * 0.1d, 45d, 1200d);
        }
    }

    private static double rms(short[] input, int count) {
        double sum = 0d;
        for (int i = 0; i < count; i++) {
            double value = input[i];
            sum += value * value;
        }
        return Math.sqrt(sum / Math.max(1, count));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

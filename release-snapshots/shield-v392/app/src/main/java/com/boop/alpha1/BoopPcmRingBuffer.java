package com.boop.alpha1;

final class BoopPcmRingBuffer {
    private final short[] samples;
    private int writeIndex;
    private int size;

    BoopPcmRingBuffer(int capacitySamples) {
        if (capacitySamples <= 0) {
            throw new IllegalArgumentException("capacitySamples must be positive");
        }
        samples = new short[capacitySamples];
    }

    synchronized void write(short[] input, int count) {
        if (input == null || count <= 0) {
            return;
        }
        int boundedCount = Math.min(count, input.length);
        int start = Math.max(0, boundedCount - samples.length);
        for (int i = start; i < boundedCount; i++) {
            samples[writeIndex] = input[i];
            writeIndex = (writeIndex + 1) % samples.length;
            if (size < samples.length) {
                size++;
            }
        }
    }

    synchronized short[] snapshot() {
        short[] result = new short[size];
        int start = size == samples.length ? writeIndex : 0;
        for (int i = 0; i < size; i++) {
            result[i] = samples[(start + i) % samples.length];
        }
        return result;
    }
}

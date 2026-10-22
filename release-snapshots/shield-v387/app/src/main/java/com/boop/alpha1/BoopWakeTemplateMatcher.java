package com.boop.alpha1;

import java.util.Arrays;

/** Additional custom-name detector fed by the same PCM stream as Sherpa. */
final class BoopWakeTemplateMatcher {
    private final BoopWakeAcousticProfile profile;
    private final int sampleRate;
    private final BoopWakeUtteranceSegmenter segmenter;
    private final int trainedSamples;
    private final int minStreamingSamples;
    private final short[] streamHistory;

    private int streamHistoryCount;
    private boolean streamSpeechActive;
    private int streamQuietChunks;
    private double streamNoiseFloor = 90d;

    BoopWakeTemplateMatcher(BoopWakeAcousticProfile profile, int sampleRate) {
        if (profile == null) throw new IllegalArgumentException("profile required");
        if (sampleRate <= 0) throw new IllegalArgumentException("sample rate required");
        this.profile = profile;
        this.sampleRate = sampleRate;
        this.segmenter = new BoopWakeUtteranceSegmenter(sampleRate);
        this.trainedSamples = Math.max(1,
                Math.round(profile.averageDurationSeconds() * sampleRate));
        this.minStreamingSamples = Math.max(sampleRate / 8,
                Math.round(trainedSamples * 0.72f));
        this.streamHistory = new short[Math.max(sampleRate, Math.round(sampleRate * 2.6f))];
    }

    boolean accept(short[] input, int count) {
        int bounded = input == null ? 0 : Math.min(Math.max(0, count), input.length);
        if (bounded <= 0) return false;

        // Keep the original silence-terminated matcher as a fallback. It remains
        // useful for deliberate wake-only utterances and preserves the proven path.
        short[] utterance = segmenter.accept(input, bounded);

        boolean streamingMatched = updateStreamingCandidate(input, bounded);
        if (streamingMatched) {
            reset();
            return true;
        }

        if (utterance != null) {
            BoopWakePronunciationFeatures.Result features =
                    BoopWakePronunciationFeatures.extract(utterance, sampleRate);
            boolean matched = features != null
                    && profile.matches(features.vector(), features.durationSeconds());
            resetStreaming();
            if (matched) {
                reset();
                return true;
            }
        }
        return false;
    }

    private boolean updateStreamingCandidate(short[] input, int count) {
        double level = rms(input, count);
        double startThreshold = Math.max(180d, streamNoiseFloor * 2.8d);
        if (!streamSpeechActive) {
            if (level < startThreshold) {
                streamNoiseFloor = streamNoiseFloor * 0.96d + level * 0.04d;
                return false;
            }
            streamSpeechActive = true;
            streamQuietChunks = 0;
            streamHistoryCount = 0;
        }

        appendStreamHistory(input, count);
        double quietThreshold = Math.max(120d, streamNoiseFloor * 1.45d);
        if (level <= quietThreshold) streamQuietChunks++;
        else streamQuietChunks = 0;

        boolean matched = false;
        if (streamHistoryCount >= minStreamingSamples) {
            int end = streamHistoryCount;
            int halfBlockEnd = Math.max(0, end - count / 2);

            // Prefix candidates let a bare learned name fire before trailing
            // silence. Target-duration suffixes also tolerate a short natural
            // lead-in without turning the matcher into a second microphone path.
            matched = matchesCandidate(0, end)
                    || matchesCandidate(0, halfBlockEnd)
                    || matchesCandidate(Math.max(0, end - trainedSamples), end)
                    || matchesCandidate(Math.max(0, halfBlockEnd - trainedSamples), halfBlockEnd);
        }

        if (matched) return true;
        if (streamQuietChunks >= 2 || streamHistoryCount >= streamHistory.length) {
            resetStreaming();
        }
        return false;
    }

    private boolean matchesCandidate(int start, int end) {
        int length = end - start;
        if (length < minStreamingSamples || end > streamHistoryCount) return false;
        short[] candidate = Arrays.copyOfRange(streamHistory, start, end);
        BoopWakePronunciationFeatures.Result features =
                BoopWakePronunciationFeatures.extract(candidate, sampleRate);
        return features != null
                && profile.matches(features.vector(), features.durationSeconds());
    }

    private void appendStreamHistory(short[] input, int count) {
        int remaining = streamHistory.length - streamHistoryCount;
        int copied = Math.min(count, Math.max(0, remaining));
        if (copied > 0) {
            System.arraycopy(input, 0, streamHistory, streamHistoryCount, copied);
            streamHistoryCount += copied;
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

    private void resetStreaming() {
        streamHistoryCount = 0;
        streamSpeechActive = false;
        streamQuietChunks = 0;
    }

    void reset() {
        segmenter.reset();
        resetStreaming();
    }
}

package com.boop.alpha1;

import java.util.Arrays;

/** Additional custom-name detector fed by the same PCM stream as Sherpa. */
final class BoopWakeTemplateMatcher {
    private static final float[] STREAM_WINDOW_RATIOS = {0.82f, 0.94f, 1.00f, 1.06f, 1.18f};

    private final BoopWakeAcousticProfile profile;
    private final int sampleRate;
    private final BoopWakeUtteranceSegmenter segmenter;
    private final int[] streamWindowSamples;
    private final int streamEndStepSamples;
    private final short[] streamHistory;
    private int streamHistoryCount;

    BoopWakeTemplateMatcher(BoopWakeAcousticProfile profile, int sampleRate) {
        if (profile == null) throw new IllegalArgumentException("profile required");
        if (sampleRate <= 0) throw new IllegalArgumentException("sample rate required");
        this.profile = profile;
        this.sampleRate = sampleRate;
        this.segmenter = new BoopWakeUtteranceSegmenter(sampleRate);

        int trainedSamples = Math.max(1,
                Math.round(profile.averageDurationSeconds() * sampleRate));
        streamWindowSamples = new int[STREAM_WINDOW_RATIOS.length];
        int largestWindow = 0;
        for (int i = 0; i < STREAM_WINDOW_RATIOS.length; i++) {
            int samples = Math.max(1, Math.round(trainedSamples * STREAM_WINDOW_RATIOS[i]));
            streamWindowSamples[i] = samples;
            largestWindow = Math.max(largestWindow, samples);
        }
        // The controller feeds 100 ms blocks. Inspect candidate endings every 25 ms
        // inside the newest block so a command that immediately follows the learned
        // name cannot hide the name boundary from this detector.
        streamEndStepSamples = Math.max(1, sampleRate / 40);
        streamHistory = new short[largestWindow + sampleRate];
    }

    boolean accept(short[] input, int count) {
        int bounded = input == null ? 0 : Math.min(Math.max(0, count), input.length);
        if (bounded <= 0) return false;

        appendStreamHistory(input, bounded);
        if (matchesStreamingWindow(bounded)) {
            reset();
            return true;
        }

        // Preserve the original silence-terminated path as a fallback for deliberate
        // wake-only utterances and for profiles that do not match an early window.
        short[] utterance = segmenter.accept(input, bounded);
        if (utterance == null) return false;
        BoopWakePronunciationFeatures.Result features =
                BoopWakePronunciationFeatures.extract(utterance, sampleRate);
        boolean matched = features != null
                && profile.matches(features.vector(), features.durationSeconds());
        if (matched) reset();
        return matched;
    }

    private boolean matchesStreamingWindow(int newestSamples) {
        int firstNewEnd = Math.max(1, streamHistoryCount - newestSamples);
        int lastEnd = streamHistoryCount;
        for (int end = firstNewEnd; end <= lastEnd; end += streamEndStepSamples) {
            if (matchesAtEnd(end)) return true;
        }
        if ((lastEnd - firstNewEnd) % streamEndStepSamples != 0 && matchesAtEnd(lastEnd)) {
            return true;
        }
        return false;
    }

    private boolean matchesAtEnd(int end) {
        for (int windowSamples : streamWindowSamples) {
            int start = end - windowSamples;
            if (start < 0 || end > streamHistoryCount) continue;
            short[] candidate = Arrays.copyOfRange(streamHistory, start, end);
            BoopWakePronunciationFeatures.Result features =
                    BoopWakePronunciationFeatures.extract(candidate, sampleRate);
            if (features != null
                    && profile.matches(features.vector(), features.durationSeconds())) {
                return true;
            }
        }
        return false;
    }

    private void appendStreamHistory(short[] input, int count) {
        if (count >= streamHistory.length) {
            System.arraycopy(input, count - streamHistory.length,
                    streamHistory, 0, streamHistory.length);
            streamHistoryCount = streamHistory.length;
            return;
        }
        int overflow = Math.max(0, streamHistoryCount + count - streamHistory.length);
        if (overflow > 0) {
            System.arraycopy(streamHistory, overflow, streamHistory, 0,
                    streamHistoryCount - overflow);
            streamHistoryCount -= overflow;
        }
        System.arraycopy(input, 0, streamHistory, streamHistoryCount, count);
        streamHistoryCount += count;
    }

    void reset() {
        segmenter.reset();
        streamHistoryCount = 0;
    }
}

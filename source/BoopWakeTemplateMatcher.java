package com.boop.alpha1;

/** Additional custom-name detector fed by the same PCM stream as Sherpa. */
final class BoopWakeTemplateMatcher {
    private final BoopWakeAcousticProfile profile;
    private final int sampleRate;
    private final BoopWakeUtteranceSegmenter segmenter;

    BoopWakeTemplateMatcher(BoopWakeAcousticProfile profile, int sampleRate) {
        if (profile == null) throw new IllegalArgumentException("profile required");
        this.profile = profile;
        this.sampleRate = sampleRate;
        this.segmenter = new BoopWakeUtteranceSegmenter(sampleRate);
    }

    boolean accept(short[] input, int count) {
        short[] utterance = segmenter.accept(input, count);
        if (utterance == null) return false;
        BoopWakePronunciationFeatures.Result features =
                BoopWakePronunciationFeatures.extract(utterance, sampleRate);
        return features != null && profile.matches(features.vector(), features.durationSeconds());
    }

    void reset() { segmenter.reset(); }
}

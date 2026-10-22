package com.boop.alpha1;

import java.util.ArrayList;
import java.util.List;

/** Collects five local pronunciations from the controller-owned PCM stream. */
final class BoopWakeEnrollmentSession {
    static final class Update {
        private final int acceptedCount;
        private final boolean retry;
        private final BoopWakeAcousticProfile profile;
        Update(int acceptedCount, boolean retry, BoopWakeAcousticProfile profile) {
            this.acceptedCount = acceptedCount;
            this.retry = retry;
            this.profile = profile;
        }
        int acceptedCount() { return acceptedCount; }
        boolean retry() { return retry; }
        BoopWakeAcousticProfile profile() { return profile; }
    }

    private final String name;
    private final int sampleRate;
    private final BoopWakeUtteranceSegmenter segmenter;
    private final List<float[]> examples = new ArrayList<>();
    private float durationSum;

    BoopWakeEnrollmentSession(String name, int sampleRate) {
        this.name = BoopWakeName.normalize(name);
        if (BoopWakeName.isDefault(this.name)) {
            throw new IllegalArgumentException("BOOP fallback does not require enrolment");
        }
        this.sampleRate = sampleRate;
        this.segmenter = new BoopWakeUtteranceSegmenter(sampleRate);
    }

    String name() { return name; }
    int acceptedCount() { return examples.size(); }

    Update accept(short[] input, int count) {
        short[] utterance = segmenter.accept(input, count);
        if (utterance == null) return null;
        BoopWakePronunciationFeatures.Result features =
                BoopWakePronunciationFeatures.extract(utterance, sampleRate);
        if (features == null) return new Update(examples.size(), true, null);

        float[] vector = features.vector();
        if (!examples.isEmpty()) {
            float closest = -1f;
            for (float[] prior : examples) {
                closest = Math.max(closest, BoopWakeAcousticProfile.cosine(prior, vector));
            }
            // Reject obvious wrong words/noise while allowing the user to vary distance and emphasis.
            if (closest < 0.52f) return new Update(examples.size(), true, null);
        }

        examples.add(vector);
        durationSum += features.durationSeconds();
        if (examples.size() < BoopWakeAcousticProfile.REQUIRED_UTTERANCES) {
            return new Update(examples.size(), false, null);
        }
        BoopWakeAcousticProfile profile = BoopWakeAcousticProfile.train(
                name, examples, durationSum / examples.size());
        return new Update(examples.size(), false, profile);
    }
}

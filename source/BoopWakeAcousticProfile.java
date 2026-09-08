package com.boop.alpha1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Compact pronunciation profile built from five local examples. No PCM is retained. */
final class BoopWakeAcousticProfile {
    static final int REQUIRED_UTTERANCES = 5;
    private static final String CODEC = "BWP1";
    private static final float MIN_THRESHOLD = 0.78f;
    private static final float MAX_THRESHOLD = 0.94f;

    private final String name;
    private final float[] centroid;
    private final float averageDurationSeconds;
    private final float threshold;

    private BoopWakeAcousticProfile(String name, float[] centroid,
            float averageDurationSeconds, float threshold) {
        this.name = BoopWakeName.normalize(name);
        this.centroid = normalize(centroid);
        this.averageDurationSeconds = averageDurationSeconds;
        this.threshold = threshold;
    }

    static BoopWakeAcousticProfile train(String name, List<float[]> examples,
            float averageDurationSeconds) {
        if (BoopWakeName.isDefault(name)) {
            throw new IllegalArgumentException("BOOP is the permanent fallback and is not user-trained");
        }
        if (examples == null || examples.size() != REQUIRED_UTTERANCES) {
            throw new IllegalArgumentException("Exactly five wake-name utterances are required");
        }
        int dimensions = examples.get(0) == null ? 0 : examples.get(0).length;
        if (dimensions <= 0) throw new IllegalArgumentException("Wake profile has no features");
        List<float[]> normalized = new ArrayList<>(REQUIRED_UTTERANCES);
        float[] centre = new float[dimensions];
        for (float[] example : examples) {
            if (example == null || example.length != dimensions) {
                throw new IllegalArgumentException("Wake profile feature dimensions differ");
            }
            float[] unit = normalize(example);
            normalized.add(unit);
            for (int i = 0; i < dimensions; i++) centre[i] += unit[i];
        }
        centre = normalize(centre);
        float weakest = 1f;
        for (float[] example : normalized) weakest = Math.min(weakest, cosine(centre, example));
        float threshold = clamp(weakest - 0.08f, MIN_THRESHOLD, MAX_THRESHOLD);
        float duration = clamp(averageDurationSeconds, 0.15f, 2.5f);
        return new BoopWakeAcousticProfile(name, centre, duration, threshold);
    }

    String name() { return name; }
    float averageDurationSeconds() { return averageDurationSeconds; }
    float threshold() { return threshold; }

    boolean matches(float[] candidate, float durationSeconds) {
        if (candidate == null || candidate.length != centroid.length || durationSeconds <= 0f) return false;
        float ratio = durationSeconds / averageDurationSeconds;
        if (ratio < 0.52f || ratio > 1.72f) return false;
        return cosine(centroid, candidate) >= threshold;
    }

    String encode() {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer vectorBytes = ByteBuffer.allocate(centroid.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : centroid) vectorBytes.putFloat(value);
        return CODEC + "|"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(nameBytes) + "|"
                + Float.toString(averageDurationSeconds) + "|"
                + Float.toString(threshold) + "|"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(vectorBytes.array());
    }

    static BoopWakeAcousticProfile decode(String encoded) {
        if (encoded == null || encoded.isBlank()) return null;
        try {
            String[] parts = encoded.split("\\|", -1);
            if (parts.length != 5 || !CODEC.equals(parts[0])) return null;
            String name = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            float duration = Float.parseFloat(parts[2]);
            float threshold = Float.parseFloat(parts[3]);
            byte[] raw = Base64.getUrlDecoder().decode(parts[4]);
            if (raw.length == 0 || raw.length % 4 != 0) return null;
            ByteBuffer bytes = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
            float[] vector = new float[raw.length / 4];
            for (int i = 0; i < vector.length; i++) vector[i] = bytes.getFloat();
            if (BoopWakeName.isDefault(name)) return null;
            return new BoopWakeAcousticProfile(name, vector,
                    clamp(duration, 0.15f, 2.5f), clamp(threshold, MIN_THRESHOLD, MAX_THRESHOLD));
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    static float cosine(float[] left, float[] right) {
        if (left == null || right == null || left.length == 0 || left.length != right.length) return -1f;
        double dot = 0d, l2 = 0d, r2 = 0d;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            l2 += left[i] * left[i];
            r2 += right[i] * right[i];
        }
        if (l2 <= 1e-12 || r2 <= 1e-12) return -1f;
        return (float) (dot / Math.sqrt(l2 * r2));
    }

    private static float[] normalize(float[] input) {
        float[] result = input.clone();
        double sum = 0d;
        for (float value : result) sum += value * value;
        if (sum <= 1e-12) return result;
        float scale = (float) (1.0 / Math.sqrt(sum));
        for (int i = 0; i < result.length; i++) result[i] *= scale;
        return result;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

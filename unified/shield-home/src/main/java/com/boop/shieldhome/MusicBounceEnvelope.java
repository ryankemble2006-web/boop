package com.boop.shieldhome;

/** Real audio level to vertical lift. Its clock is deliberately unrelated to blink speed. */
public final class MusicBounceEnvelope {
    public static final float MAX_HEIGHT_FRACTION = 0.14f;
    private float height;
    private long previousMs = -1L;

    public static float levelOf(byte[] waveform) {
        if (waveform == null || waveform.length < 2) return 0f;
        double sum = 0, squares = 0;
        for (byte sample : waveform) {
            double value = (sample & 255) - 128;
            sum += value;
            squares += value * value;
        }
        // Reject DC, including a vendor's constant zero buffer, rather than treating it as music.
        double mean = sum / waveform.length;
        double rms = Math.sqrt(Math.max(0, squares / waveform.length - mean * mean)) / 128.0;
        if (rms < 0.006) return 0f;
        return (float) Math.min(1, Math.sqrt(rms * 1.5));
    }

    public float update(float level, long nowMs) {
        float safe = Float.isFinite(level) ? Math.max(0f, Math.min(1f, level)) : 0f;
        long elapsed = previousMs < 0 ? 33L : Math.max(0L, Math.min(250L, nowMs - previousMs));
        previousMs = Math.max(previousMs, nowMs);
        float target = safe * MAX_HEIGHT_FRACTION;
        double tauMs = target > height ? 28.0 : 140.0;
        height += (target - height) * (float) (1.0 - Math.exp(-elapsed / tauMs));
        height = Math.max(0f, Math.min(MAX_HEIGHT_FRACTION, height));
        if (target == 0f && height < 0.0005f) height = 0f;
        return height;
    }

    public void reset() {
        height = 0f;
        previousMs = -1L;
    }
}

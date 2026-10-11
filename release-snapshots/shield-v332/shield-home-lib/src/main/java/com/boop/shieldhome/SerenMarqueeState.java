package com.boop.shieldhome;

/** Scroll time belongs to the displayed text, not to feed refreshes or layouts. */
final class SerenMarqueeState {
    static final float SPEED_DP_PER_SECOND = 30f * 1.05f;
    private String text = "";
    private long elapsedMs, lastFrameMs = -1;

    boolean setText(String next) {
        if (text.equals(next)) return false;
        text = next;
        elapsedMs = 0;
        lastFrameMs = -1;
        return true;
    }

    void tick(long nowMs) {
        if (lastFrameMs >= 0) elapsedMs += Math.max(0, nowMs - lastFrameMs);
        lastFrameMs = nowMs;
    }

    void pause() { lastFrameMs = -1; }

    float position(float lineWidth, float viewportWidth, float density) {
        if (lineWidth <= viewportWidth || viewportWidth <= 0) return 0;
        // Same one-third-viewport gap and 1200ms repeat pause as Android's marquee.
        double distance = lineWidth + viewportWidth / 3f;
        double pixelsPerMs = SPEED_DP_PER_SECOND * density / 1000d;
        double phase = elapsedMs % (distance / pixelsPerMs + 1200d);
        return (float) Math.min(distance, phase * pixelsPerMs);
    }
}

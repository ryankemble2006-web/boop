package com.boop.shieldhome;

/** Pure geometry for the approved one-piece top eyelid. */
final class NowPlayingPuppetLidTravel {
    private static final float MIN_BLINK_OPENNESS = 0.05f;
    private static final float REST_CENTRE = 0.22f;
    private static final float REST_SIDE = 0.52f;
    private static final float CLOSED_CENTRE = 1.08f;
    private static final float CLOSED_SIDE = 1.12f;

    private NowPlayingPuppetLidTravel() { }

    static float closureForOpenness(float openness) {
        float safe = Float.isFinite(openness) ? clamp(openness) : 1f;
        return clamp((1f - safe) / (1f - MIN_BLINK_OPENNESS));
    }

    static float centreEdge(float openness) {
        return lerp(REST_CENTRE, CLOSED_CENTRE, closureForOpenness(openness));
    }

    static float sideEdge(float openness) {
        return lerp(REST_SIDE, CLOSED_SIDE, closureForOpenness(openness));
    }

    private static float lerp(float from, float to, float amount) {
        return from + (to - from) * amount;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}

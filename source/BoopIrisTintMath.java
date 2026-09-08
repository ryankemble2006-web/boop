package com.boop.alpha1;

/** Colour arithmetic for the approved permanent eye master. The PNG itself is never changed. */
final class BoopIrisTintMath {
    private BoopIrisTintMath() { }

    static boolean inIris(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return false;
        double ax = (x + 0.5) * 1774.0 / width;
        double ay = (y + 0.5) * 887.0 / height;
        return inRing(ax, ay, 522.0) || inRing(ax, ay, 1204.0);
    }

    private static boolean inRing(double x, double y, double centreX) {
        double dx = x - centreX;
        double dy = y - 529.0;
        // Restrict colour changes to the blue/cyan iris annulus. The pupil,
        // highlights, white sclera, eyelids and supplied alpha stay untouched.
        return dx * dx / (205.0 * 205.0) + dy * dy / (205.0 * 205.0) < 1.0
                && dx * dx / (148.0 * 148.0) + dy * dy / (153.0 * 153.0) > 1.0;
    }

    static int tint(int argb, int requestedHue) {
        int hue = Math.max(0, Math.min(359, requestedHue));
        if (hue == 190 || (argb >>> 24) == 0) return argb;
        float r = ((argb >>> 16) & 255) / 255f;
        float g = ((argb >>> 8) & 255) / 255f;
        float b = (argb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float saturation = max == 0f ? 0f : delta / max;
        if (saturation < .30f || max < .18f || delta == 0f) return argb;
        float originalHue;
        if (max == r) originalHue = 60f * ((g - b) / delta);
        else if (max == g) originalHue = 60f * ((b - r) / delta + 2f);
        else originalHue = 60f * ((r - g) / delta + 4f);
        if (originalHue < 0f) originalHue += 360f;
        if (originalHue < 155f || originalHue > 235f) return argb;
        float shifted = (originalHue + hue - 190f + 360f) % 360f;
        float chroma = max * saturation;
        float intermediate = chroma * (1f - Math.abs((shifted / 60f) % 2f - 1f));
        float offset = max - chroma;
        float rr, gg, bb;
        if (shifted < 60f) { rr = chroma; gg = intermediate; bb = 0f; }
        else if (shifted < 120f) { rr = intermediate; gg = chroma; bb = 0f; }
        else if (shifted < 180f) { rr = 0f; gg = chroma; bb = intermediate; }
        else if (shifted < 240f) { rr = 0f; gg = intermediate; bb = chroma; }
        else if (shifted < 300f) { rr = intermediate; gg = 0f; bb = chroma; }
        else { rr = chroma; gg = 0f; bb = intermediate; }
        return (argb & 0xff000000) | (channel(rr + offset) << 16)
                | (channel(gg + offset) << 8) | channel(bb + offset);
    }

    private static int channel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255f)));
    }
}

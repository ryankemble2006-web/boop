package com.boop.shieldoverlay;

/** Full-screen Deezer puppet geometry. Keeps the existing H1 motion asset centred
 * on a black BOOP-owned canvas while leaving input pass-through unchanged. */
final class FullscreenDeezerGeometry {
    private static final double SINE = Math.sin(Math.toRadians(1.8));
    private static final double LEFT = -550 - 460 * SINE - 6 - 2;
    private static final double RIGHT = 532 + 460 * SINE + 6 + 2;
    private static final double TOP = -460 - 550 * SINE - 9 - 2;
    private static final double BOTTOM = 322 + 550 * SINE + 2;
    private static final double ENVELOPE_WIDTH = RIGHT - LEFT;
    private static final double ENVELOPE_HEIGHT = BOTTOM - TOP;
    private static final float PIVOT_VISUAL_CENTRE_OFFSET_Y = 68f;

    private FullscreenDeezerGeometry() {}

    static HeadphoneGeometry.Layout calculate(int displayWidth, int displayHeight) {
        int width = Math.max(1, displayWidth);
        int height = Math.max(1, displayHeight);

        // Roughly Wall-BOOP scale on a 16:9 TV: large enough to own the canvas,
        // while the measured H1 nod/sway envelope remains safely inside the screen.
        float scale = (float) Math.min(
                (width * 0.58) / ENVELOPE_WIDTH,
                (height * 0.78) / ENVELOPE_HEIGHT);
        scale = Math.max(0.000001f, scale);

        float originX = width / 2f;
        float originY = height / 2f + PIVOT_VISUAL_CENTRE_OFFSET_Y * scale;
        return new HeadphoneGeometry.Layout(width, height, 0, 0, scale, originX, originY);
    }
}

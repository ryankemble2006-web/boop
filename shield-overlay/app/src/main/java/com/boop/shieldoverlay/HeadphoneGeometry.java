package com.boop.shieldoverlay;

public final class HeadphoneGeometry {
    // Source: 1536x1024 RGBA; alpha bounds [218,120,1300,902], pivot (768,580).
    // For +/-1.8 degrees, abs(sin) <= sin(1.8), abs(cos) <= 1.
    // Add +/-6 sway, -9..0 bounce and two source pixels of filtering clearance.
    private static final double SINE = Math.sin(Math.toRadians(1.8));
    private static final double LEFT = -550 - 460 * SINE - 6 - 2;
    private static final double RIGHT = 532 + 460 * SINE + 6 + 2;
    private static final double TOP = -460 - 550 * SINE - 9 - 2;
    private static final double BOTTOM = 322 + 550 * SINE + 2;

    private HeadphoneGeometry() {}

    public static Layout calculate(int displayWidth, int displayHeight) {
        int displayW = Math.max(1, displayWidth);
        int displayH = Math.max(1, displayHeight);
        int marginX = Math.min(displayW - 1, (int) Math.ceil(displayW * .03));
        int marginY = Math.min(displayH - 1, (int) Math.ceil(displayH * .03));
        double envelopeW = RIGHT - LEFT;
        double envelopeH = BOTTOM - TOP;
        double scale = Math.min(displayW * .14 / 686.0,
                Math.min((displayW - marginX) / envelopeW, (displayH - marginY) / envelopeH));
        int width = Math.min(displayW - marginX, Math.max(1, (int) Math.ceil(envelopeW * scale)));
        int height = Math.min(displayH - marginY, Math.max(1, (int) Math.ceil(envelopeH * scale)));
        float originX = (float) (-LEFT * scale);
        float originY = (float) (-TOP * scale);
        // Keep the original size/envelope. Place the eye pivot at 25% across, 35% down.
        // The overlay keeps TOP | END gravity: x is an end inset, not a left offset.
        int left = (int) Math.round(displayW * .25 - originX);
        int x = Math.max(0, Math.min(displayW - width, displayW - width - left));
        int y = Math.max(0, Math.min(displayH - height,
                (int) Math.round(displayH * .35 - originY)));
        return new Layout(width, height, x, y, (float) scale, originX, originY);
    }
    public static final class Layout {
        public final int width, height, x, y;
        public final float scale, originX, originY;
        Layout(int width, int height, int x, int y, float scale, float originX, float originY) {
            this.width = width; this.height = height; this.x = x; this.y = y;
            this.scale = scale; this.originX = originX; this.originY = originY;
        }
    }
}

package com.boop.shieldhdrdebug;

final class OverlayGeometry {
    private static final float EYE_WIDTH_FRACTION = 0.14f;
    private static final float EYE_RIGHT_INSET_FRACTION = 0.03f;
    private static final float EYE_TOP_INSET_FRACTION = 0.03f;
    private static final float EYE_PAIR_WIDTH = 764f;
    private static final float EYE_PAIR_HEIGHT = 393f;

    private static final float PANEL_WIDTH_FRACTION = 0.52f;
    private static final float PANEL_HEIGHT_FRACTION = 0.92f;
    private static final float PANEL_LEFT_INSET_FRACTION = 0.02f;
    private static final float PANEL_TOP_INSET_FRACTION = 0.03f;

    private OverlayGeometry() { }

    static Geometry eyes(int displayWidth, int displayHeight) {
        int width = Math.max(1, Math.round(displayWidth * EYE_WIDTH_FRACTION));
        int height = Math.max(1, Math.round(width * (EYE_PAIR_HEIGHT / EYE_PAIR_WIDTH)));
        int x = Math.max(0, Math.round(displayWidth * EYE_RIGHT_INSET_FRACTION));
        int y = Math.max(0, Math.round(displayHeight * EYE_TOP_INSET_FRACTION));
        return new Geometry(width, height, x, y);
    }

    static Geometry panel(int displayWidth, int displayHeight) {
        int width = Math.max(1, Math.round(displayWidth * PANEL_WIDTH_FRACTION));
        int height = Math.max(1, Math.round(displayHeight * PANEL_HEIGHT_FRACTION));
        int x = Math.max(0, Math.round(displayWidth * PANEL_LEFT_INSET_FRACTION));
        int y = Math.max(0, Math.round(displayHeight * PANEL_TOP_INSET_FRACTION));
        return new Geometry(width, height, x, y);
    }

    static final class Geometry {
        final int width;
        final int height;
        final int x;
        final int y;

        Geometry(int width, int height, int x, int y) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }
    }
}

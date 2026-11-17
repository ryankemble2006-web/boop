package com.boop.shieldoverlay;

final class OverlayGeometry {
    private static final float WIDTH_FRACTION = 0.14f;
    private static final float RIGHT_INSET_FRACTION = 0.03f;
    private static final float TOP_INSET_FRACTION = 0.03f;
    private static final float PAIR_WIDTH = 764f;
    private static final float PAIR_HEIGHT = 393f;

    private OverlayGeometry() { }

    static Geometry calculate(int displayWidth, int displayHeight) {
        int width = Math.max(1, Math.round(displayWidth * WIDTH_FRACTION));
        int height = Math.max(1, Math.round(width * (PAIR_HEIGHT / PAIR_WIDTH)));
        int x = Math.max(0, Math.round(displayWidth * RIGHT_INSET_FRACTION));
        int y = Math.max(0, Math.round(displayHeight * TOP_INSET_FRACTION));
        return new Geometry(width, height, x, y);
    }

    static final class Geometry {
        private final int width;
        private final int height;
        private final int x;
        private final int y;

        Geometry(int width, int height, int x, int y) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }

        int width() {
            return width;
        }

        int height() {
            return height;
        }

        int x() {
            return x;
        }

        int y() {
            return y;
        }
    }
}

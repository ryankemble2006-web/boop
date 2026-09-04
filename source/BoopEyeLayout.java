package com.boop.alpha1;

final class BoopEyeLayout {
    static final float SOURCE_WIDTH = 941f;
    static final float SOURCE_EYE_CENTRE_DISTANCE = 435f;
    static final float SOURCE_EYE_WIDTH = 329f;
    static final float SOURCE_EYE_HEIGHT = 393f;
    static final float LANDSCAPE_EYE_SCALE = 1.20f;

    private BoopEyeLayout() { }

    static Layout calculate(int width, int height) {
        if (width <= height) {
            return new Layout(false, null, null);
        }

        float baseScale = Math.min(width, height) / SOURCE_WIDTH;
        float centreDistance = SOURCE_EYE_CENTRE_DISTANCE * baseScale;
        float eyeWidth = SOURCE_EYE_WIDTH * baseScale * LANDSCAPE_EYE_SCALE;
        float eyeHeight = SOURCE_EYE_HEIGHT * baseScale * LANDSCAPE_EYE_SCALE;
        float centreX = width / 2f;
        float centreY = height / 2f;

        Eye left = new Eye(
                centreX - (centreDistance / 2f),
                centreY,
                eyeWidth,
                eyeHeight);
        Eye right = new Eye(
                centreX + (centreDistance / 2f),
                centreY,
                eyeWidth,
                eyeHeight);
        return new Layout(true, left, right);
    }

    static final class Layout {
        private final boolean landscape;
        private final Eye left;
        private final Eye right;

        Layout(boolean landscape, Eye left, Eye right) {
            this.landscape = landscape;
            this.left = left;
            this.right = right;
        }

        boolean landscape() {
            return landscape;
        }

        Eye left() {
            return left;
        }

        Eye right() {
            return right;
        }
    }

    static final class Eye {
        private final float centerX;
        private final float centerY;
        private final float width;
        private final float height;

        Eye(float centerX, float centerY, float width, float height) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }

        float centerX() {
            return centerX;
        }

        float centerY() {
            return centerY;
        }

        float width() {
            return width;
        }

        float height() {
            return height;
        }
    }
}

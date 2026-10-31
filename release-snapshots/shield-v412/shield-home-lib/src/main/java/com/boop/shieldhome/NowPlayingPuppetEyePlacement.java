package com.boop.shieldhome;

/**
 * Immutable pair geometry for the permanently approved BOOP eyes master.
 * The pair is transformed as one piece so relative scale, level and spacing cannot drift.
 */
final class NowPlayingPuppetEyePlacement {
    static final float MASTER_WIDTH = 1774f;
    static final float MASTER_HEIGHT = 887f;

    private static final float PAIR_CENTRE_X = 734.5f;
    private static final float PAIR_CENTRE_Y = 655f;
    private static final float PAIR_HEIGHT = 385f;

    private NowPlayingPuppetEyePlacement() { }

    static Box pairInHeadphoneSource() {
        float width = PAIR_HEIGHT * MASTER_WIDTH / MASTER_HEIGHT;
        return new Box(
                PAIR_CENTRE_X - width / 2f,
                PAIR_CENTRE_Y - PAIR_HEIGHT / 2f,
                PAIR_CENTRE_X + width / 2f,
                PAIR_CENTRE_Y + PAIR_HEIGHT / 2f);
    }

    static Box leftEyeInHeadphoneSource() {
        return masterBoxToHeadphones(102f, 61f, 825f, 828f);
    }

    static Box rightEyeInHeadphoneSource() {
        return masterBoxToHeadphones(947f, 61f, 1670f, 828f);
    }

    private static Box masterBoxToHeadphones(float left, float top, float right, float bottom) {
        Box pair = pairInHeadphoneSource();
        float scale = pair.height() / MASTER_HEIGHT;
        return new Box(
                pair.left + left * scale,
                pair.top + top * scale,
                pair.left + right * scale,
                pair.top + bottom * scale);
    }

    static final class Box {
        final float left;
        final float top;
        final float right;
        final float bottom;

        Box(float left, float top, float right, float bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        float width() { return right - left; }
        float height() { return bottom - top; }
        float centreX() { return (left + right) / 2f; }
        float centreY() { return (top + bottom) / 2f; }
    }
}

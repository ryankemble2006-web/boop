package com.boop.alpha1;

import android.graphics.Bitmap;
import java.util.Arrays;

/** Reuses one bitmap and one small pixel tile while the hue slider moves. */
final class BoopIrisTint {
    private final Bitmap original;
    private final int left, top, width, height;
    private int[] originalTile;
    private int[] tile;
    private int[] irisIndices;
    private Bitmap tinted;
    private int lastHue = -1;

    BoopIrisTint(Bitmap original) {
        this.original = original;
        left = original == null ? 0 : (int) Math.floor(original.getWidth() * 200.0 / 941.0);
        top = original == null ? 0 : (int) Math.floor(original.getHeight() * 741.0 / 1672.0);
        int right = original == null ? 0 : (int) Math.ceil(original.getWidth() * 748.0 / 941.0);
        int bottom = original == null ? 0 : (int) Math.ceil(original.getHeight() * 945.0 / 1672.0);
        width = right - left;
        height = bottom - top;
    }

    Bitmap forHue(int requestedHue) {
        int hue = BoopEyeHueMath.clampHue(requestedHue);
        if (original == null || hue == BoopEyeHueMath.DEFAULT_HUE_DEGREES) return original;
        if (width <= 0 || height <= 0) return original;
        if (tinted == null) prepare();
        if (tinted == null) return original;
        if (hue != lastHue) {
            for (int index : irisIndices) tile[index] = BoopIrisTintMath.tint(originalTile[index], hue);
            tinted.setPixels(tile, 0, width, left, top, width, height);
            lastHue = hue;
        }
        return tinted;
    }

    private void prepare() {
        originalTile = new int[width * height];
        original.getPixels(originalTile, 0, width, left, top, width, height);
        tile = originalTile.clone();
        int[] indices = new int[width * height];
        int count = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (BoopIrisTintMath.inIris(left + x, top + y, original.getWidth(), original.getHeight())
                        && BoopIrisTintMath.tint(originalTile[index], 0) != originalTile[index]) {
                    indices[count++] = index;
                }
            }
        }
        irisIndices = Arrays.copyOf(indices, count);
        tinted = original.copy(Bitmap.Config.ARGB_8888, true);
    }
}

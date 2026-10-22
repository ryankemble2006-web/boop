package com.boop.shieldhome;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/** Rotates the existing arrows without changing their artwork or measured bounds. */
final class SyncArrowDrawable extends Drawable {
    private final Drawable arrows;
    SyncArrowDrawable(Drawable arrows) { this.arrows = arrows; }
    @Override public void draw(Canvas canvas) {
        int saved = canvas.save();
        Rect bounds = getBounds();
        canvas.rotate(getLevel() * 0.036f, bounds.exactCenterX(), bounds.exactCenterY());
        arrows.draw(canvas);
        canvas.restoreToCount(saved);
    }
    @Override protected void onBoundsChange(Rect bounds) { arrows.setBounds(bounds); }
    @Override protected boolean onLevelChange(int level) { invalidateSelf(); return true; }
    @Override public void setAlpha(int alpha) { arrows.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter filter) { arrows.setColorFilter(filter); invalidateSelf(); }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return arrows.getIntrinsicWidth(); }
    @Override public int getIntrinsicHeight() { return arrows.getIntrinsicHeight(); }
}

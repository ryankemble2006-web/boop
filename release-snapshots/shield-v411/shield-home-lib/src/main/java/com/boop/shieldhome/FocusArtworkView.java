package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.ImageView;

/** Clip image pixels inside the focus ring; the foreground itself keeps the full outer shape. */
final class FocusArtworkView extends ImageView {
    private final Path clip = new Path();
    FocusArtworkView(Context context) { super(context); }
    @Override protected void onDraw(Canvas canvas) {
        int saved = canvas.save();
        if (getForeground() instanceof FocusChrome.ArtworkOutline) {
            FocusChrome.ArtworkOutline ring = (FocusChrome.ArtworkOutline)getForeground();
            RectF inner = new RectF(ring.strokePx, ring.strokePx,
                    getWidth() - ring.strokePx, getHeight() - ring.strokePx);
            float radius = Math.max(0, ring.radiusPx - ring.strokePx);
            clip.reset(); clip.addRoundRect(inner, radius, radius, Path.Direction.CW);
            canvas.clipPath(clip);
        }
        super.onDraw(canvas); canvas.restoreToCount(saved);
    }
}

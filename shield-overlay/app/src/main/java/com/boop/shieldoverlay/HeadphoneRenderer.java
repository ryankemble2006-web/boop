package com.boop.shieldoverlay;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

final class HeadphoneRenderer {
    private final Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect(0, 0, 1536, 1024);
    private final RectF destination = new RectF(-768f, -580f, 768f, 444f);

    HeadphoneRenderer(Resources resources) {
        bitmap = BitmapFactory.decodeResource(resources, R.drawable.boop_headphones);
    }

    void draw(Canvas canvas, HeadphoneGeometry.Layout layout, long sampleTimeMs) {
        if (bitmap == null || layout == null) {
            return;
        }
        // The clock already supplies the approved 1.2x time; do not scale it again.
        MediaPuppetMotion.Pose pose = MediaPuppetMotion.music(sampleTimeMs);
        int save = canvas.save();
        canvas.translate(layout.originX, layout.originY);
        canvas.scale(layout.scale, layout.scale);
        canvas.translate(pose.x, pose.y);
        canvas.rotate(pose.rotationDegrees);
        canvas.drawBitmap(bitmap, source, destination, paint);
        canvas.restoreToCount(save);
    }
}

package com.boop.alpha1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

final class BoopFaceView extends View {
    static final Rect LEFT_SOURCE = new Rect(90, 600, 419, 993);
    static final Rect RIGHT_SOURCE = new Rect(525, 600, 854, 993);

    private final Bitmap faceBitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    BoopFaceView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        if (faceBitmap == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(getWidth(), getHeight());
        if (!layout.landscape()) {
            drawPortraitFace(canvas);
            return;
        }

        drawEye(canvas, LEFT_SOURCE, layout.left());
        drawEye(canvas, RIGHT_SOURCE, layout.right());
    }

    private void drawPortraitFace(Canvas canvas) {
        float scale = Math.min(
                getWidth() / (float) faceBitmap.getWidth(),
                getHeight() / (float) faceBitmap.getHeight());
        float width = faceBitmap.getWidth() * scale;
        float height = faceBitmap.getHeight() * scale;
        float left = (getWidth() - width) / 2f;
        float top = (getHeight() - height) / 2f;

        canvas.drawBitmap(
                faceBitmap,
                null,
                new RectF(left, top, left + width, top + height),
                paint);
    }

    private void drawEye(Canvas canvas, Rect source, BoopEyeLayout.Eye eye) {
        float halfWidth = eye.width() / 2f;
        float halfHeight = eye.height() / 2f;
        RectF destination = new RectF(
                eye.centerX() - halfWidth,
                eye.centerY() - halfHeight,
                eye.centerX() + halfWidth,
                eye.centerY() + halfHeight);
        canvas.drawBitmap(faceBitmap, source, destination, paint);
    }
}

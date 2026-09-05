package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.OvershootInterpolator;

final class BoopOverlayView extends View {
    private static final Rect LEFT_SOURCE = new Rect(90, 600, 419, 993);
    private static final Rect RIGHT_SOURCE = new Rect(525, 600, 854, 993);
    private static final int PAIR_WIDTH = 764;
    private static final int PAIR_HEIGHT = 393;
    private static final int RIGHT_OFFSET = 435;
    private static final float IDLE_SCALE_Y = 0.08f;
    private static final long WAKE_DURATION_MS = 380L;
    private static final int BACKGROUND_THRESHOLD = 32;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap leftEye;
    private final Bitmap rightEye;

    BoopOverlayView(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        Bitmap source = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
        leftEye = isolateEye(source, LEFT_SOURCE);
        rightEye = isolateEye(source, RIGHT_SOURCE);
    }

    void wakeOnce() {
        animate().cancel();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleX(1f);
        setScaleY(IDLE_SCALE_Y);
        setAlpha(1f);
        animate()
                .scaleY(1f)
                .setDuration(WAKE_DURATION_MS)
                .setInterpolator(new OvershootInterpolator(0.45f))
                .start();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        setPivotX(width / 2f);
        setPivotY(height / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        float scale = Math.min(getWidth() / (float) PAIR_WIDTH, getHeight() / (float) PAIR_HEIGHT);
        float renderedWidth = PAIR_WIDTH * scale;
        float renderedHeight = PAIR_HEIGHT * scale;
        float left = (getWidth() - renderedWidth) / 2f;
        float top = (getHeight() - renderedHeight) / 2f;

        RectF leftDestination = new RectF(
                left,
                top,
                left + LEFT_SOURCE.width() * scale,
                top + renderedHeight);
        RectF rightDestination = new RectF(
                left + RIGHT_OFFSET * scale,
                top,
                left + (RIGHT_OFFSET + RIGHT_SOURCE.width()) * scale,
                top + renderedHeight);

        if (leftEye != null) {
            canvas.drawBitmap(leftEye, null, leftDestination, paint);
        }
        if (rightEye != null) {
            canvas.drawBitmap(rightEye, null, rightDestination, paint);
        }
    }

    private static Bitmap isolateEye(Bitmap source, Rect crop) {
        if (source == null || crop.right > source.getWidth() || crop.bottom > source.getHeight()) {
            return null;
        }

        Bitmap eye = Bitmap.createBitmap(source, crop.left, crop.top, crop.width(), crop.height())
                .copy(Bitmap.Config.ARGB_8888, true);
        int width = eye.getWidth();
        int height = eye.getHeight();
        int[] pixels = new int[width * height];
        eye.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] queue = new int[pixels.length];
        int head = 0;
        int tail = 0;

        for (int x = 0; x < width; x++) {
            tail = enqueueBackground(pixels, queue, tail, x);
            tail = enqueueBackground(pixels, queue, tail, (height - 1) * width + x);
        }
        for (int y = 1; y < height - 1; y++) {
            tail = enqueueBackground(pixels, queue, tail, y * width);
            tail = enqueueBackground(pixels, queue, tail, y * width + width - 1);
        }

        while (head < tail) {
            int index = queue[head++];
            int x = index % width;
            int y = index / width;
            if (x > 0) {
                tail = enqueueBackground(pixels, queue, tail, index - 1);
            }
            if (x + 1 < width) {
                tail = enqueueBackground(pixels, queue, tail, index + 1);
            }
            if (y > 0) {
                tail = enqueueBackground(pixels, queue, tail, index - width);
            }
            if (y + 1 < height) {
                tail = enqueueBackground(pixels, queue, tail, index + width);
            }
        }

        eye.setPixels(pixels, 0, width, 0, 0, width, height);
        return eye;
    }

    private static int enqueueBackground(int[] pixels, int[] queue, int tail, int index) {
        int pixel = pixels[index];
        if (!isBoundaryBackground(pixel)) {
            return tail;
        }
        pixels[index] = Color.TRANSPARENT;
        queue[tail] = index;
        return tail + 1;
    }

    private static boolean isBoundaryBackground(int pixel) {
        int alpha = (pixel >>> 24) & 0xff;
        if (alpha == 0) {
            return false;
        }
        int red = (pixel >>> 16) & 0xff;
        int green = (pixel >>> 8) & 0xff;
        int blue = pixel & 0xff;
        return red <= BACKGROUND_THRESHOLD
                && green <= BACKGROUND_THRESHOLD
                && blue <= BACKGROUND_THRESHOLD;
    }
}

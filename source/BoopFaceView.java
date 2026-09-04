package com.boop.alpha1;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

final class BoopFaceView extends View {
    static final Rect LEFT_SOURCE = new Rect(90, 600, 419, 993);
    static final Rect RIGHT_SOURCE = new Rect(525, 600, 854, 993);

    private static final float IDLE_SCALE_Y = 0.08f;
    private static final long WAKE_DURATION_MS = 380L;
    private static final long SLEEP_DURATION_MS = 300L;
    private static final long MEMBER_BERRY_DURATION_MS = 300L;

    private final Bitmap faceBitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    BoopFaceView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
    }

    void showIdleBlackImmediately() {
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(IDLE_SCALE_Y);
        setAlpha(0f);
    }

    void wakeFromIdle() {
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(IDLE_SCALE_Y);
        setAlpha(1f);
        animate()
                .scaleY(1f)
                .setDuration(WAKE_DURATION_MS)
                .setInterpolator(new OvershootInterpolator(0.45f))
                .start();
    }

    void goIdleBlack() {
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        animate()
                .alpha(0f)
                .scaleY(IDLE_SCALE_Y)
                .setDuration(SLEEP_DURATION_MS)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    void playMemberBerry(int variant) {
        if (getAlpha() <= 0f) {
            return;
        }

        animate().cancel();
        resetPuppetTransform();
        ObjectAnimator animator;
        switch (Math.floorMod(variant, 3)) {
            case 0:
                animator = ObjectAnimator.ofPropertyValuesHolder(
                        this,
                        PropertyValuesHolder.ofFloat("scaleX", 1f, 1.10f, 0.96f, 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f, 0.94f, 1.05f, 1f),
                        PropertyValuesHolder.ofFloat("rotation", 0f, -1.8f, 1.0f, 0f));
                break;
            case 1:
                animator = ObjectAnimator.ofPropertyValuesHolder(
                        this,
                        PropertyValuesHolder.ofFloat("rotationY", 0f, -14f, 10f, 0f),
                        PropertyValuesHolder.ofFloat("rotationX", 0f, 5f, -3f, 0f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f, 1.03f, 0.99f, 1f));
                break;
            default:
                animator = ObjectAnimator.ofPropertyValuesHolder(
                        this,
                        PropertyValuesHolder.ofFloat("translationX", 0f, -12f, 9f, -4f, 0f),
                        PropertyValuesHolder.ofFloat("rotation", 0f, -2.4f, 1.7f, -0.7f, 0f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f, 0.97f, 1.03f, 1f));
                break;
        }
        animator.setDuration(MEMBER_BERRY_DURATION_MS);
        animator.setInterpolator(new DecelerateInterpolator(1.1f));
        animator.start();
    }

    private void resetPuppetTransform() {
        setScaleX(1f);
        setScaleY(1f);
        setRotation(0f);
        setRotationX(0f);
        setRotationY(0f);
        setTranslationX(0f);
        setTranslationY(0f);
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

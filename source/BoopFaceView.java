package com.boop.alpha1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
    private static final long THINKING_DURATION_MS = 1680L;
    private static final long SHAKE_MUPPET_DURATION_MS = 1_050L;

    private final Bitmap faceBitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private ObjectAnimator thinkingAnimator;
    private ValueAnimator shakeAnimator;
    private boolean shakeMuppetActive;
    private float shakeFraction;
    private float shakeStrength;

    BoopFaceView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
    }

    void showIdleBlackImmediately() {
        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(IDLE_SCALE_Y);
        setAlpha(0f);
    }

    void wakeFromIdle() {
        stopThinking();
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
        stopThinking();
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

        stopThinking();
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

    void playShakeMuppet(float strength) {
        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(1f);
        setAlpha(1f);

        shakeStrength = Math.max(0f, Math.min(1f, strength));
        shakeFraction = 0f;
        shakeMuppetActive = true;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        shakeAnimator = animator;
        animator.setDuration(SHAKE_MUPPET_DURATION_MS);
        animator.addUpdateListener(valueAnimator -> {
            shakeFraction = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (shakeAnimator != animation) {
                    return;
                }
                shakeAnimator = null;
                shakeMuppetActive = false;
                shakeFraction = 0f;
                invalidate();
            }
        });
        animator.start();
    }

    void startThinking() {
        if (thinkingAnimator != null && thinkingAnimator.isRunning()) {
            return;
        }

        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setAlpha(1f);

        // A tiny puppet "hmm": cock the head, peek the other way, then settle.
        // Deliberately organic rather than a spinner/loading UI.
        thinkingAnimator = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat(
                        "rotation", 0f, -2.6f, -1.3f, 2.1f, 0.8f, 0f),
                PropertyValuesHolder.ofFloat(
                        "rotationY", 0f, -10f, -5f, 8f, 3f, 0f),
                PropertyValuesHolder.ofFloat(
                        "translationX", 0f, -8f, -4f, 7f, 3f, 0f),
                PropertyValuesHolder.ofFloat(
                        "translationY", 0f, -4f, -2f, -5f, -1f, 0f),
                PropertyValuesHolder.ofFloat(
                        "scaleX", 1f, 1.018f, 1.008f, 1.014f, 1.004f, 1f),
                PropertyValuesHolder.ofFloat(
                        "scaleY", 1f, 0.975f, 0.99f, 1.018f, 1.006f, 1f));
        thinkingAnimator.setDuration(THINKING_DURATION_MS);
        thinkingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        thinkingAnimator.setRepeatCount(ValueAnimator.INFINITE);
        thinkingAnimator.setRepeatMode(ValueAnimator.RESTART);
        thinkingAnimator.start();
    }

    void stopThinking() {
        if (thinkingAnimator != null) {
            thinkingAnimator.cancel();
            thinkingAnimator = null;
        }
        cancelShakeMuppet();
        resetPuppetTransform();
        setAlpha(1f);
    }

    private void cancelShakeMuppet() {
        ValueAnimator animator = shakeAnimator;
        shakeAnimator = null;
        if (animator != null) {
            animator.cancel();
        }
        shakeMuppetActive = false;
        shakeFraction = 0f;
        invalidate();
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
        if (shakeMuppetActive) {
            EyeGeometry leftBase;
            EyeGeometry rightBase;
            if (layout.landscape()) {
                leftBase = EyeGeometry.from(layout.left());
                rightBase = EyeGeometry.from(layout.right());
            } else {
                leftBase = portraitEyeGeometry(LEFT_SOURCE);
                rightBase = portraitEyeGeometry(RIGHT_SOURCE);
            }
            drawShakeEye(canvas, LEFT_SOURCE, leftBase, true);
            drawShakeEye(canvas, RIGHT_SOURCE, rightBase, false);
            return;
        }
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

    private EyeGeometry portraitEyeGeometry(Rect source) {
        float scale = Math.min(
                getWidth() / (float) faceBitmap.getWidth(),
                getHeight() / (float) faceBitmap.getHeight());
        float faceWidth = faceBitmap.getWidth() * scale;
        float faceHeight = faceBitmap.getHeight() * scale;
        float faceLeft = (getWidth() - faceWidth) / 2f;
        float faceTop = (getHeight() - faceHeight) / 2f;
        return new EyeGeometry(
                faceLeft + source.exactCenterX() * scale,
                faceTop + source.exactCenterY() * scale,
                source.width() * scale,
                source.height() * scale);
    }

    private void drawShakeEye(Canvas canvas, Rect source, EyeGeometry base, boolean leftEye) {
        BoopShakeEyeMotion.Pose pose = BoopShakeEyeMotion.pose(
                leftEye,
                shakeFraction,
                getWidth(),
                getHeight(),
                base.centerX,
                base.centerY,
                base.width,
                base.height,
                shakeStrength);
        float halfWidth = base.width * pose.scaleX() / 2f;
        float halfHeight = base.height * pose.scaleY() / 2f;
        RectF destination = new RectF(
                pose.centerX() - halfWidth,
                pose.centerY() - halfHeight,
                pose.centerX() + halfWidth,
                pose.centerY() + halfHeight);
        int save = canvas.save();
        canvas.rotate(pose.rotation(), pose.centerX(), pose.centerY());
        canvas.drawBitmap(faceBitmap, source, destination, paint);
        canvas.restoreToCount(save);
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

    private static final class EyeGeometry {
        final float centerX;
        final float centerY;
        final float width;
        final float height;

        EyeGeometry(float centerX, float centerY, float width, float height) {
            this.centerX = centerX;
            this.centerY = centerY;
            this.width = width;
            this.height = height;
        }

        static EyeGeometry from(BoopEyeLayout.Eye eye) {
            return new EyeGeometry(eye.centerX(), eye.centerY(), eye.width(), eye.height());
        }
    }
}

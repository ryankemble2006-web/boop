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
    static final Rect LEFT_SOURCE = new Rect(BoopApprovedEyeGeometry.LEFT_SOURCE);
    static final Rect RIGHT_SOURCE = new Rect(BoopApprovedEyeGeometry.RIGHT_SOURCE);

    private static final float IDLE_SCALE_Y = 0.08f;
    private static final long WAKE_DURATION_MS = 380L;
    private static final long SLEEP_DURATION_MS = 300L;
    private static final long MEMBER_BERRY_DURATION_MS = 300L;
    private static final long THINKING_DURATION_MS = 1680L;
    private static final long SHAKE_MUPPET_DURATION_MS = 1_050L;

    private final Bitmap originalFaceBitmap;
    private Bitmap faceBitmap;
    private BoopIrisTint irisTint;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private ObjectAnimator thinkingAnimator;
    private ValueAnimator shakeAnimator;
    private boolean shakeMuppetActive;
    private float shakeFraction;
    private float shakeStrength;
    // BOOP_IDLE_BLINK_V1: separate eyelid geometry, not a whole-view animator.
    private final java.util.Random idleBlinkRandom = new java.util.Random();
    private java.util.function.BooleanSupplier idleBlinkAllowed;
    private final Runnable idleBlinkRunnable = this::runIdleBlink;
    private ValueAnimator idleBlinkAnimator;
    // BOOP_SLEEP_CHARM_V1: accepted blink lead-in, then slow drowsy close.
    private ValueAnimator sleepCharmAnimator;
    private int idleBlinksRemaining;
    private float idleBlinkOpenness = 1f;
    private boolean awakeForBlink;
    private long idleBlinkBlockedUntil;

    // BOOP_LISTENING_EYES_V1: recognizer lifecycle remains the listening-state owner.
    // BOOP_PROCEDURAL_IRISES_V3: approved PNG supplies whites/lids; irises are Canvas geometry.
    private static final Rect FULL_FACE_SOURCE = new Rect(0, 0, 1774, 887);
    private static final float LEFT_IRIS_SOURCE_X = 535f;
    private static final float RIGHT_IRIS_SOURCE_X = 1233f;
    private static final float IRIS_SOURCE_Y = 543f;
    private static final float IRIS_RADIUS_X = 205f;
    private static final float IRIS_RADIUS_Y = 205f;
    private static final float PUPIL_RADIUS_X = 148f;
    private static final float PUPIL_RADIUS_Y = 153f;
    // BOOP_PROCEDURAL_IRIS_CLEAN_SCLERA_V4: cover the complete reading-motion envelope.
    private static final float BASE_CLEAN_RADIUS_X = 300f;
    private static final float BASE_CLEAN_RADIUS_Y = 280f;
    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();
    private final android.graphics.Paint proceduralIrisPaint =
            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
    private final android.graphics.Paint proceduralPupilPaint =
            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
    private final android.graphics.Paint proceduralHighlightPaint =
            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
    private Bitmap proceduralBaseBitmap;
    private int proceduralIrisHueDegrees = BoopEyeHueMath.DEFAULT_HUE_DEGREES;
    private ValueAnimator listeningCueAnimator;
    private float listeningCueFraction;

    BoopFaceView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        originalFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
        faceBitmap = originalFaceBitmap;
        setEyeHueDegrees(BoopEyeHue.loadHue(context));
    }

    void setIdleBlinkAllowed(java.util.function.BooleanSupplier allowed) {
        idleBlinkAllowed = allowed;
    }

    private boolean canIdleBlink() {
        return awakeForBlink && isAttachedToWindow() && hasWindowFocus()
                && getWindowVisibility() == VISIBLE && isShown() && getAlpha() > 0f
                && android.os.SystemClock.uptimeMillis() >= idleBlinkBlockedUntil
                && !shakeMuppetActive
                && (thinkingAnimator == null || !thinkingAnimator.isRunning())
                && idleBlinkAllowed != null && idleBlinkAllowed.getAsBoolean()
                && ValueAnimator.areAnimatorsEnabled();
    }

    private void startIdleBlinking(long settlingMillis) {
        awakeForBlink = true;
        deferIdleBlink(settlingMillis);
    }

    private void deferIdleBlink(long settlingMillis) {
        idleBlinkBlockedUntil = android.os.SystemClock.uptimeMillis() + settlingMillis;
        idleBlinksRemaining = 0;
        cancelIdleBlinkFrame();
        queueNextIdleBlink();
    }

    private void queueNextIdleBlink() {
        if (idleBlinkRunnable == null) return;
        removeCallbacks(idleBlinkRunnable);
        if (awakeForBlink && isAttachedToWindow() && hasWindowFocus()
                && getWindowVisibility() == VISIBLE && isShown()) {
            postDelayed(idleBlinkRunnable, BoopIdleBlink.nextDelayMillis(idleBlinkRandom));
        }
    }

    private void runIdleBlink() {
        removeCallbacks(idleBlinkRunnable);
        if (!canIdleBlink()) {
            idleBlinksRemaining = 0;
            queueNextIdleBlink();
            return;
        }
        if (idleBlinksRemaining <= 0) {
            idleBlinksRemaining = BoopIdleBlink.shouldDoubleBlink(idleBlinkRandom) ? 2 : 1;
        }
        idleBlinksRemaining--;
        cancelIdleBlinkFrame();
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        idleBlinkAnimator = animator;
        animator.setDuration(BoopIdleBlink.DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (!canIdleBlink()) {
                idleBlinksRemaining = 0;
                cancelIdleBlinkFrame();
                queueNextIdleBlink();
                return;
            }
            idleBlinkOpenness = BoopIdleBlink.openness((float) frame.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator finished) {
                if (idleBlinkAnimator != finished) return;
                idleBlinkAnimator = null;
                idleBlinkOpenness = 1f;
                invalidate();
                if (!canIdleBlink()) {
                    idleBlinksRemaining = 0;
                    queueNextIdleBlink();
                    return;
                }
                if (idleBlinksRemaining > 0) {
                    postDelayed(idleBlinkRunnable, BoopIdleBlink.DOUBLE_GAP_MS);
                } else {
                    queueNextIdleBlink();
                }
            }
        });
        animator.start();
    }

    private void cancelIdleBlinkFrame() {
        ValueAnimator running = idleBlinkAnimator;
        idleBlinkAnimator = null; // Cancellation must not queue a replacement.
        if (running != null) running.cancel();
        idleBlinkOpenness = 1f;
        invalidate();
    }

    private void suspendIdleBlinking() {
        if (idleBlinkRunnable != null) removeCallbacks(idleBlinkRunnable);
        idleBlinksRemaining = 0;
        cancelIdleBlinkFrame();
    }

    private void stopIdleBlinking() {
        awakeForBlink = false;
        suspendIdleBlinking();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        queueNextIdleBlink();
    }

    @Override protected void onDetachedFromWindow() {
        suspendIdleBlinking();
        super.onDetachedFromWindow();
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) queueNextIdleBlink();
        else suspendIdleBlinking();
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) queueNextIdleBlink();
        else suspendIdleBlinking();
    }

    private void cancelSleepCharm() {
        ValueAnimator running = sleepCharmAnimator;
        sleepCharmAnimator = null;
        if (running != null) running.cancel();
        idleBlinkOpenness = 1f;
        setAlpha(1f);
        invalidate();
    }

    void setEyeHueDegrees(int hueDegrees) {
        proceduralIrisHueDegrees = BoopEyeHueMath.clampHue(hueDegrees);
        faceBitmap = originalFaceBitmap;
        paint.setColorFilter(null);
        if (proceduralBaseBitmap == null && originalFaceBitmap != null) {
            proceduralBaseBitmap = buildProceduralEyeBase(originalFaceBitmap);
        }
        invalidate();
    }

    void startListeningCue() {
        if (!listeningCueState.start()) return;
        cancelIdleBlinkFrame();
        listeningCueFraction = 0f;
        if (!ValueAnimator.areAnimatorsEnabled()) {
            listeningCueFraction = 0.5f;
            invalidate();
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        listeningCueAnimator = animator;
        animator.setDuration(BoopListeningGaze.HALF_SWEEP_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(frame -> {
            if (listeningCueAnimator != frame || !listeningCueState.isActive()) return;
            listeningCueFraction = (float) frame.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    void stopListeningCue() {
        boolean changed = listeningCueState.stop();
        ValueAnimator animator = listeningCueAnimator;
        listeningCueAnimator = null;
        if (animator != null) animator.cancel();
        if (changed || listeningCueFraction != 0f) {
            listeningCueFraction = 0f;
            invalidate();
        }
    }

    private float listeningCueZoom() {
        return listeningCueState.isActive() ? BoopListeningGaze.zoom() : 1f;
    }

    private Bitmap proceduralBase() {
        if (proceduralBaseBitmap == null && originalFaceBitmap != null) {
            proceduralBaseBitmap = buildProceduralEyeBase(originalFaceBitmap);
        }
        return proceduralBaseBitmap != null ? proceduralBaseBitmap : faceBitmap;
    }

    private Bitmap buildProceduralEyeBase(Bitmap source) {
        if (source == null) return null;
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Canvas baseCanvas = new Canvas(base);
        android.graphics.Paint cleanPaint =
                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        float sx = base.getWidth() / 1774f;
        float sy = base.getHeight() / 887f;
        float[] centres = new float[] { LEFT_IRIS_SOURCE_X, RIGHT_IRIS_SOURCE_X };
        for (float sourceX : centres) {
            float cx = sourceX * sx;
            float cy = IRIS_SOURCE_Y * sy;
            float rx = BASE_CLEAN_RADIUS_X * sx;
            float ry = BASE_CLEAN_RADIUS_Y * sy;
            // BOOP_PROCEDURAL_IRIS_FEATHERED_SCLERA_V5: fully hide the old
            // baked-in iris, then fade into the approved sclera instead of
            // leaving an opaque white contact-lens edge.
            int white = android.graphics.Color.argb(255, 252, 252, 252);
            int mid = android.graphics.Color.argb(255, 249, 250, 252);
            int innerEdge = android.graphics.Color.argb(255, 247, 249, 251);
            int feather = android.graphics.Color.argb(150, 247, 249, 251);
            int clear = android.graphics.Color.argb(0, 247, 249, 251);
            cleanPaint.setShader(new android.graphics.RadialGradient(
                    cx, cy, Math.min(rx, ry),
                    new int[] { white, mid, innerEdge, feather, clear },
                    new float[] { 0f, 0.55f, 0.82f, 0.92f, 1f },
                    android.graphics.Shader.TileMode.CLAMP));
            baseCanvas.drawOval(new RectF(cx - rx, cy - ry, cx + rx, cy + ry), cleanPaint);
            cleanPaint.setShader(null);
        }
        return base;
    }

    private int irisColour(float saturation, float value) {
        return android.graphics.Color.HSVToColor(new float[] {
                proceduralIrisHueDegrees, saturation, value });
    }

    private void drawProceduralIris(
            Canvas canvas, Rect source, RectF destination, float irisSourceX) {
        if (source.width() <= 0 || source.height() <= 0) return;
        float scaleX = destination.width() / source.width();
        float scaleY = destination.height() / source.height();
        float offsetX = listeningCueState.isActive()
                ? BoopListeningGaze.horizontalSourceOffset(listeningCueFraction) : 0f;
        float offsetY = listeningCueState.isActive()
                ? BoopListeningGaze.verticalSourceOffset() : 0f;
        float cx = destination.left + (irisSourceX + offsetX - source.left) * scaleX;
        float cy = destination.top + (IRIS_SOURCE_Y + offsetY - source.top) * scaleY;
        float irisRx = IRIS_RADIUS_X * scaleX;
        float irisRy = IRIS_RADIUS_Y * scaleY;

        int dark = irisColour(0.94f, 0.30f);
        int body = irisColour(0.92f, 0.72f);
        int bright = irisColour(0.88f, 1.00f);
        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.FILL);
        proceduralIrisPaint.setShader(new android.graphics.LinearGradient(
                cx, cy - irisRy, cx, cy + irisRy,
                new int[] { dark, body, bright },
                new float[] { 0f, 0.52f, 1f },
                android.graphics.Shader.TileMode.CLAMP));
        RectF iris = new RectF(cx - irisRx, cy - irisRy, cx + irisRx, cy + irisRy);
        canvas.drawOval(iris, proceduralIrisPaint);
        proceduralIrisPaint.setShader(null);
        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.STROKE);
        proceduralIrisPaint.setStrokeWidth(Math.max(2f, 7f * Math.min(scaleX, scaleY)));
        proceduralIrisPaint.setColor(irisColour(0.95f, 0.55f));
        canvas.drawOval(iris, proceduralIrisPaint);
        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.FILL);

        proceduralPupilPaint.setColor(android.graphics.Color.BLACK);
        canvas.drawOval(new RectF(
                cx - PUPIL_RADIUS_X * scaleX,
                cy - PUPIL_RADIUS_Y * scaleY,
                cx + PUPIL_RADIUS_X * scaleX,
                cy + PUPIL_RADIUS_Y * scaleY), proceduralPupilPaint);

        proceduralHighlightPaint.setColor(android.graphics.Color.WHITE);
        canvas.drawOval(new RectF(
                cx - 104f * scaleX, cy - 124f * scaleY,
                cx - 30f * scaleX, cy - 40f * scaleY), proceduralHighlightPaint);
        canvas.drawCircle(
                cx - 88f * scaleX, cy - 20f * scaleY,
                Math.max(2f, 10f * Math.min(scaleX, scaleY)), proceduralHighlightPaint);
    }

    void showIdleBlackImmediately() {
        cancelSleepCharm();
        stopListeningCue();
        stopIdleBlinking();
        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(IDLE_SCALE_Y);
        setAlpha(0f);
    }

    void wakeFromIdle() {
        cancelSleepCharm();
        startIdleBlinking(WAKE_DURATION_MS);
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
        stopListeningCue();
        stopIdleBlinking();
        stopThinking();
        animate().cancel();
        cancelSleepCharm();
        resetPuppetTransform();
        setPivotX(getWidth() / 2f);
        setPivotY(getHeight() / 2f);
        setScaleY(1f);
        setAlpha(1f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        sleepCharmAnimator = animator;
        animator.setDuration(BoopSleepCharm.TOTAL_DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (sleepCharmAnimator != animator) return;
            float progress = (float) frame.getAnimatedValue();
            idleBlinkOpenness = BoopSleepCharm.openness(progress);
            setAlpha(BoopSleepCharm.alpha(progress));
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator finished) {
                if (sleepCharmAnimator != finished) return;
                sleepCharmAnimator = null;
                idleBlinkOpenness = 1f;
                resetPuppetTransform();
                setPivotX(getWidth() / 2f);
                setPivotY(getHeight() / 2f);
                setScaleY(IDLE_SCALE_Y);
                setAlpha(0f);
                invalidate();
            }
        });
        animator.start();
    }

    void playMemberBerry(int variant) {
        if (getAlpha() <= 0f) {
            return;
        }

        stopThinking();
        animate().cancel();
        resetPuppetTransform();
        deferIdleBlink(MEMBER_BERRY_DURATION_MS);
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
        startIdleBlinking(SHAKE_MUPPET_DURATION_MS);
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
        stopListeningCue();
        deferIdleBlink(THINKING_DURATION_MS);
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
        cancelIdleBlinkFrame();
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
            EyeGeometry leftBase = EyeGeometry.from(layout.left());
            EyeGeometry rightBase = EyeGeometry.from(layout.right());
            drawShakeEye(canvas, LEFT_SOURCE, leftBase, true);
            drawShakeEye(canvas, RIGHT_SOURCE, rightBase, false);
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

        int eyelidSave = canvas.save();
        float eyelidCenterY = top + LEFT_SOURCE.exactCenterY() * scale;
        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);
        float listeningZoom = listeningCueZoom();
        canvas.scale(listeningZoom, listeningZoom, getWidth() / 2f, getHeight() / 2f);
        RectF faceDestination = new RectF(left, top, left + width, top + height);
        Bitmap eyeBase = proceduralBase();
        if (eyeBase != null) canvas.drawBitmap(eyeBase, null, faceDestination, paint);
        drawProceduralIris(canvas, FULL_FACE_SOURCE, faceDestination, LEFT_IRIS_SOURCE_X);
        drawProceduralIris(canvas, FULL_FACE_SOURCE, faceDestination, RIGHT_IRIS_SOURCE_X);
        canvas.restoreToCount(eyelidSave);
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
        float listeningZoom = listeningCueZoom();
        float halfWidth = eye.width() * listeningZoom / 2f;
        float halfHeight = eye.height() * idleBlinkOpenness * listeningZoom / 2f;
        RectF destination = new RectF(
                eye.centerX() - halfWidth,
                eye.centerY() - halfHeight,
                eye.centerX() + halfWidth,
                eye.centerY() + halfHeight);
        Bitmap eyeBase = proceduralBase();
        if (eyeBase != null) canvas.drawBitmap(eyeBase, source, destination, paint);
        float irisSourceX = source.left < BoopApprovedEyeGeometry.CANVAS_WIDTH / 2
                ? LEFT_IRIS_SOURCE_X : RIGHT_IRIS_SOURCE_X;
        drawProceduralIris(canvas, source, destination, irisSourceX);
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

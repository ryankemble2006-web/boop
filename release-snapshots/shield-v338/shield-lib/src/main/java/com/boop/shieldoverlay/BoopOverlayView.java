package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Choreographer;
import android.view.View;
import android.view.animation.OvershootInterpolator;

final class BoopOverlayView extends View {
    private static final Rect LEFT_SOURCE = new Rect(BoopApprovedEyeGeometry.LEFT_SOURCE);
    private static final Rect RIGHT_SOURCE = new Rect(BoopApprovedEyeGeometry.RIGHT_SOURCE);
    private static final float IDLE_SCALE_Y = 0.08f;
    private static final long WAKE_DURATION_MS = 380L;
    private static final int BACKGROUND_THRESHOLD = 32;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap faceBitmap;
    // BOOP_SHARED_SHIELD_EYES_V2
    private final java.util.Random eyeBlinkRandom = new java.util.Random();
    private final Runnable eyeBlinkRunnable = this::runEyeBlink;
    private android.animation.ValueAnimator eyeBlinkAnimator;
    private float eyeBlinkOpenness = 1f;
    private boolean eyeBlinkQueued;
    private final HeadphoneRenderer headphoneRenderer;
    private final MediaPuppetFrameLoop frameLoop;
    private HeadphoneGeometry.Layout headphoneLayout;
    private DeezerPuppetPolicy.Mode puppetMode = DeezerPuppetPolicy.Mode.EYES;
    private Integer playbackState;
    private long puppetSessionId;
    private long sampleTimeMs;
    private long settleStartedAtMs = -1L;
    private long trackAccentStartedAtMs = -1L;
    private MediaPuppetMotion.Pose settleFrom = FullscreenPuppetMotion.rest();
    private boolean attached;
    private boolean displayActive;
    private boolean animationObservationAvailable;
    private boolean powerObservationAvailable;

    BoopOverlayView(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);
        headphoneRenderer = new HeadphoneRenderer(getResources());
        frameLoop = new MediaPuppetFrameLoop(new FrameScheduler(), SystemClock::uptimeMillis,
                sample -> {
                    sampleTimeMs = sample;
                    invalidate();
                });
        refreshAnimationPreference();
    }

    void wakeOnce() {
        // A queued creation-time wake may run after a Deezer snapshot has arrived.
        if (puppetMode != DeezerPuppetPolicy.Mode.EYES) {
            return;
        }
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

    void setPuppetSnapshot(MediaPuppetState.Snapshot snapshot) {
        DeezerPuppetPolicy.Mode oldMode = puppetMode;
        Integer oldPlaybackState = playbackState;
        if (snapshot.mode != DeezerPuppetPolicy.Mode.EYES
                && oldMode == DeezerPuppetPolicy.Mode.EYES) {
            animate().cancel();
            setScaleX(1f);
            setScaleY(1f);
            setAlpha(1f);
        }

        puppetMode = snapshot.mode;
        playbackState = snapshot.playbackState;
        long now = SystemClock.uptimeMillis();

        if (puppetSessionId != snapshot.sessionId) {
            puppetSessionId = snapshot.sessionId;
            frameLoop.reset();
            clearActingBeat();
        }

        if (puppetMode == DeezerPuppetPolicy.Mode.EYES) {
            clearActingBeat();
        } else if (puppetMode == DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING) {
            // Resume the accumulated groove clock rather than restarting the dance.
            clearActingBeat();
        } else if (isTrackChangeState(playbackState)
                && !isTrackChangeState(oldPlaybackState)) {
            trackAccentStartedAtMs = now;
            settleStartedAtMs = -1L;
        } else if (isPausedState(playbackState)
                && oldMode == DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING) {
            // Finish the current pose with a short, eased settle instead of snapping to rest.
            settleFrom = FullscreenPuppetMotion.groove(sampleTimeMs);
            settleStartedAtMs = now;
            trackAccentStartedAtMs = -1L;
        }

        updateFrameLoop();
        invalidate();
    }

    private static boolean isPausedState(Integer state) {
        return state != null && state == 2;
    }

    private static boolean isTrackChangeState(Integer state) {
        return state != null && (state == 9 || state == 10 || state == 11);
    }

    private void clearActingBeat() {
        settleStartedAtMs = -1L;
        trackAccentStartedAtMs = -1L;
        settleFrom = FullscreenPuppetMotion.rest();
    }

    void setHeadphoneLayout(HeadphoneGeometry.Layout layout) {
        headphoneLayout = layout;
        invalidate();
    }

    void setDisplayActive(boolean active) {
        displayActive = active;
        updateFrameLoop();
    }

    void refreshAnimationPreference() {
        updateFrameLoop();
    }

    void setPowerObservationAvailable(boolean available) {
        powerObservationAvailable = available;
        updateFrameLoop();
    }

    void setAnimationObservationAvailable(boolean available) {
        animationObservationAvailable = available;
        updateFrameLoop();
    }

    private boolean powerSaveActiveOrUnknown() {
        if (!powerObservationAvailable) {
            return true;
        }
        PowerManager manager = getContext().getSystemService(PowerManager.class);
        return manager == null || manager.isPowerSaveMode();
    }

    private void updateFrameLoop() {
        if (frameLoop != null) {
            frameLoop.update(puppetMode,
                    attached && isShown() && getWindowVisibility() == VISIBLE && displayActive,
                    animationObservationAvailable,
                    // ValueAnimator's process cache can lag this setting's observer notification.
                    () -> Settings.Global.getFloat(getContext().getContentResolver(),
                            Settings.Global.ANIMATOR_DURATION_SCALE, 1f),
                    this::powerSaveActiveOrUnknown);
        }
        syncEyeBlink();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        refreshAnimationPreference();
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        frameLoop.detach();
        stopEyeBlink();
        animate().cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        updateFrameLoop();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        updateFrameLoop();
    }

    private static final class FrameScheduler implements MediaPuppetFrameLoop.Scheduler {
        private final Choreographer choreographer = Choreographer.getInstance();
        private Runnable posted;
        private Choreographer.FrameCallback callback;

        @Override
        public void post(Runnable frame) {
            posted = frame;
            callback = time -> {
                if (posted == frame) {
                    posted = null;
                    callback = null;
                }
                frame.run();
            };
            choreographer.postFrameCallback(callback);
        }

        @Override
        public void cancel(Runnable frame) {
            if (posted == frame && callback != null) {
                choreographer.removeFrameCallback(callback);
                posted = null;
                callback = null;
            }
        }
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

        if (puppetMode != DeezerPuppetPolicy.Mode.EYES) {
            // Full-screen music-puppet mode: BOOP owns the picture while Deezer
            // continues to own playback underneath. The overlay window remains
            // NOT_FOCUSABLE + NOT_TOUCHABLE, so Shield/Deezer remote input passes through.
            canvas.drawColor(Color.BLACK);
            headphoneRenderer.draw(canvas, headphoneLayout, currentPuppetPose());
            return;
        }

        drawLockedEyes(canvas);
    }

    private boolean eyeBlinkAllowed() {
        // The overlay never takes focus. Visibility/display are the lifecycle gates.
        android.os.PowerManager manager = getContext().getSystemService(android.os.PowerManager.class);
        boolean powerSave = manager != null && manager.isPowerSaveMode();
        return attached && displayActive && isShown()
                && getWindowVisibility() == VISIBLE && getAlpha() > 0f
                && puppetMode == DeezerPuppetPolicy.Mode.EYES
                && !powerSave
                && android.animation.ValueAnimator.areAnimatorsEnabled()
                && Settings.Global.getFloat(getContext().getContentResolver(),
                        Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f;
    }

    private void syncEyeBlink() {
        if (eyeBlinkRunnable == null) return; // View construction can call visibility hooks.
        if (!eyeBlinkAllowed()) {
            stopEyeBlink();
        } else if (!eyeBlinkQueued && eyeBlinkAnimator == null) {
            eyeBlinkQueued = true;
            postDelayed(eyeBlinkRunnable, BoopIdleBlink.nextDelayMillis(eyeBlinkRandom));
        }
    }

    private void runEyeBlink() {
        eyeBlinkQueued = false;
        if (!eyeBlinkAllowed()) {
            syncEyeBlink();
            return;
        }
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(0f, 1f);
        eyeBlinkAnimator = animator;
        animator.setDuration(BoopIdleBlink.DURATION_MS);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        animator.addUpdateListener(frame -> {
            if (!eyeBlinkAllowed()) {
                stopEyeBlink();
                return;
            }
            eyeBlinkOpenness = BoopIdleBlink.openness((float) frame.getAnimatedValue());
            invalidate();
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator finished) {
                if (eyeBlinkAnimator != finished) return;
                eyeBlinkAnimator = null;
                eyeBlinkOpenness = 1f;
                invalidate();
                syncEyeBlink();
            }
        });
        animator.start();
    }

    private void stopEyeBlink() {
        if (eyeBlinkRunnable != null) removeCallbacks(eyeBlinkRunnable);
        eyeBlinkQueued = false;
        android.animation.ValueAnimator animator = eyeBlinkAnimator;
        eyeBlinkAnimator = null;
        if (animator != null) animator.cancel();
        if (eyeBlinkOpenness != 1f) {
            eyeBlinkOpenness = 1f;
            invalidate();
        }
    }

    private void drawLockedEyes(Canvas canvas) {
        // Use the same approved geometry as Wall, uniformly framed inside the TV slot.
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(
                Math.max(getWidth(), getHeight() + 1), getHeight());
        if (!layout.landscape() || faceBitmap == null) return;
        BoopEyeLayout.Eye left = layout.left();
        BoopEyeLayout.Eye right = layout.right();
        float pairLeft = left.centerX() - left.width() / 2f;
        float pairRight = right.centerX() + right.width() / 2f;
        float pairWidth = pairRight - pairLeft;
        float pairHeight = Math.max(left.height(), right.height());
        if (pairWidth <= 0f || pairHeight <= 0f) return;
        float fit = Math.min(getWidth() / pairWidth, getHeight() / pairHeight);
        float centreX = (pairLeft + pairRight) / 2f;
        float centreY = left.centerY();
        int save = canvas.save();
        canvas.translate(getWidth() / 2f, getHeight() / 2f);
        canvas.scale(fit, fit);
        canvas.translate(-centreX, -centreY);
        drawLockedEye(canvas, LEFT_SOURCE, left);
        drawLockedEye(canvas, RIGHT_SOURCE, right);
        canvas.restoreToCount(save);
    }

    private void drawLockedEye(Canvas canvas, Rect source, BoopEyeLayout.Eye eye) {
        if (faceBitmap == null) return;
        float halfWidth = eye.width() / 2f;
        float halfHeight = eye.height() * eyeBlinkOpenness / 2f;
        canvas.drawBitmap(faceBitmap, source, new RectF(
                eye.centerX() - halfWidth, eye.centerY() - halfHeight,
                eye.centerX() + halfWidth, eye.centerY() + halfHeight), paint);
    }

    private MediaPuppetMotion.Pose currentPuppetPose() {
        if (puppetMode == DeezerPuppetPolicy.Mode.HEADPHONES_PLAYING) {
            return FullscreenPuppetMotion.groove(sampleTimeMs);
        }

        long now = SystemClock.uptimeMillis();
        if (trackAccentStartedAtMs >= 0L) {
            long elapsed = now - trackAccentStartedAtMs;
            if (elapsed < FullscreenPuppetMotion.TRACK_CHANGE_DURATION_MS) {
                if (attached && isShown() && displayActive) {
                    postInvalidateOnAnimation();
                }
                return FullscreenPuppetMotion.trackChange(elapsed);
            }
            trackAccentStartedAtMs = -1L;
        }

        if (settleStartedAtMs >= 0L) {
            long elapsed = now - settleStartedAtMs;
            if (elapsed < FullscreenPuppetMotion.SETTLE_DURATION_MS) {
                if (attached && isShown() && displayActive) {
                    postInvalidateOnAnimation();
                }
                return FullscreenPuppetMotion.settle(settleFrom, elapsed);
            }
            settleStartedAtMs = -1L;
        }
        return FullscreenPuppetMotion.rest();
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

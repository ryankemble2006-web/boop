package com.boop.shieldhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

/** Transparent launcher-owned headphones BOOP layer. It never participates in remote focus. */
public final class ShieldNowPlayingPuppetView extends FrameLayout {
    private static final long FRAME_MS = 33L;

    // These match ShieldHomeView's fixed HOME/nav/card geometry and ShieldNowPlayingView's
    // reserved right-hand mascot bay. The bay itself clips motion, so BOOP can never cover media UI.
    private static final int BAY_HEIGHT_DP = 154;
    private static final int BAY_RIGHT_MARGIN_DP = 60;
    private static final int BAY_TOP_MARGIN_DP = 118;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final BlinkingPuppetImageView puppet;
    private final PowerManager powerManager;
    private final Random blinkRandom = new Random();

    private NowPlayingSnapshot snapshot;
    private NowPlayingPuppetPolicy.Mode mode = NowPlayingPuppetPolicy.Mode.HIDDEN;
    private long motionStartedMs;
    private long acknowledgementStartedMs = -1L;
    private boolean frameScheduled;
    private boolean homeVisible = true;
    private ValueAnimator blinkAnimator;
    private boolean blinkScheduled;
    private boolean secondBlinkScheduled;
    private int blinksRemaining;

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!shouldAnimateFrame()) return;
            long now = SystemClock.uptimeMillis();
            NowPlayingPuppetMotion.Pose pose = currentPose(now);
            if (acknowledgementStartedMs >= 0L) {
                long ackElapsed = now - acknowledgementStartedMs;
                pose = NowPlayingPuppetMotion.acknowledge(pose, ackElapsed);
                if (ackElapsed >= NowPlayingPuppetMotion.ACK_DURATION_MS) {
                    acknowledgementStartedMs = -1L;
                }
            }
            applyPose(pose);
            if (isContinuousMotionMode() || acknowledgementStartedMs >= 0L) {
                scheduleFrame();
            }
        }
    };

    private final Runnable blinkRunnable = new Runnable() {
        @Override public void run() {
            blinkScheduled = false;
            if (!canBlink()) return;
            blinksRemaining = NowPlayingPuppetBlink.shouldDoubleBlink(blinkRandom) ? 2 : 1;
            startNextBlink();
        }
    };

    private final Runnable secondBlinkRunnable = new Runnable() {
        @Override public void run() {
            secondBlinkScheduled = false;
            if (!canBlink()) {
                blinksRemaining = 0;
                return;
            }
            startNextBlink();
        }
    };

    public ShieldNowPlayingPuppetView(Context context) {
        this(context, null);
    }

    public ShieldNowPlayingPuppetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(android.graphics.Color.TRANSPARENT);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setClickable(false);
        setLongClickable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setClipChildren(true);
        setClipToPadding(true);

        powerManager = context.getSystemService(PowerManager.class);
        puppet = new BlinkingPuppetImageView(context);
        puppet.setImageResource(com.boop.shieldhome.R.drawable.boop_headphones);
        puppet.setScaleType(ImageView.ScaleType.FIT_CENTER);
        puppet.setFocusable(false);
        puppet.setClickable(false);
        puppet.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(puppet, puppetLayout());
        setVisibility(GONE);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lockToMascotBay();
    }

    public void setSnapshot(NowPlayingSnapshot next) {
        NowPlayingSnapshot previous = snapshot;
        NowPlayingPuppetPolicy.Mode previousMode = mode;
        snapshot = next;
        mode = NowPlayingPuppetPolicy.mode(next);
        if (mode != previousMode) {
            motionStartedMs = 0L;
        }
        boolean changed = previous != null && next != null
                && (previous.sessionId() != next.sessionId()
                    || !previous.trackKey().equals(next.trackKey()));
        if (changed) {
            acknowledgementStartedMs = SystemClock.uptimeMillis();
        }
        applyCurrentState();
    }

    /** Page visibility is independent of media state so leaving HOME never forgets playback. */
    public void setHomeVisible(boolean visible) {
        if (homeVisible == visible) {
            if (visible) applyCurrentState();
            return;
        }
        homeVisible = visible;
        applyCurrentState();
    }

    private void applyCurrentState() {
        if (!homeVisible || mode == NowPlayingPuppetPolicy.Mode.HIDDEN) {
            if (mode == NowPlayingPuppetPolicy.Mode.HIDDEN) {
                acknowledgementStartedMs = -1L;
                motionStartedMs = 0L;
                applyPose(NowPlayingPuppetMotion.rest());
            }
            cancelBlink();
            stopFrames();
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        long now = SystemClock.uptimeMillis();
        if (isContinuousMotionMode() && motionStartedMs == 0L) {
            motionStartedMs = now;
        } else if (!isContinuousMotionMode()) {
            motionStartedMs = 0L;
        }

        if (!animationAllowed()) {
            acknowledgementStartedMs = -1L;
            cancelBlink();
            stopFrames();
            applyPose(NowPlayingPuppetMotion.rest());
            return;
        }

        ensureBlinkScheduled();
        NowPlayingPuppetMotion.Pose pose = currentPose(now);
        if (acknowledgementStartedMs >= 0L) {
            long ackElapsed = now - acknowledgementStartedMs;
            pose = NowPlayingPuppetMotion.acknowledge(pose, ackElapsed);
            if (ackElapsed >= NowPlayingPuppetMotion.ACK_DURATION_MS) {
                acknowledgementStartedMs = -1L;
            }
        }
        applyPose(pose);
        if (isContinuousMotionMode() || acknowledgementStartedMs >= 0L) {
            scheduleFrame();
        } else {
            stopFrames();
        }
    }

    @Override protected void onDetachedFromWindow() {
        cancelBlink();
        stopFrames();
        super.onDetachedFromWindow();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;
        int width = Math.max(1, Math.round(w * 1.10f));
        int height = Math.max(1, Math.round(h * 1.10f));
        LayoutParams params = new LayoutParams(width, height, Gravity.CENTER);
        puppet.setLayoutParams(params);
    }

    private void lockToMascotBay() {
        android.view.ViewGroup.LayoutParams current = getLayoutParams();
        if (!(current instanceof FrameLayout.LayoutParams)) {
            return;
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) current;
        int width = dp(ShieldNowPlayingView.MASCOT_BAY_DP);
        int height = dp(BAY_HEIGHT_DP);
        int right = dp(BAY_RIGHT_MARGIN_DP);
        int top = dp(BAY_TOP_MARGIN_DP);
        if (params.width == width
                && params.height == height
                && params.gravity == (Gravity.END | Gravity.TOP)
                && params.rightMargin == right
                && params.topMargin == top) {
            return;
        }
        params.width = width;
        params.height = height;
        params.gravity = Gravity.END | Gravity.TOP;
        params.rightMargin = right;
        params.topMargin = top;
        setLayoutParams(params);
    }

    private LayoutParams puppetLayout() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
    }

    private NowPlayingPuppetMotion.Pose currentPose(long now) {
        long elapsed = motionStartedMs == 0L ? 0L : Math.max(0L, now - motionStartedMs);
        if (mode == NowPlayingPuppetPolicy.Mode.GROOVE) {
            return NowPlayingPuppetMotion.groove(elapsed);
        }
        if (mode == NowPlayingPuppetPolicy.Mode.UPSET) {
            return NowPlayingPuppetMotion.upset(elapsed);
        }
        return NowPlayingPuppetMotion.rest();
    }

    private boolean isContinuousMotionMode() {
        return mode == NowPlayingPuppetPolicy.Mode.GROOVE
                || mode == NowPlayingPuppetPolicy.Mode.UPSET;
    }

    private boolean shouldAnimateFrame() {
        return homeVisible
                && getVisibility() == VISIBLE
                && mode != NowPlayingPuppetPolicy.Mode.HIDDEN
                && animationAllowed()
                && (isContinuousMotionMode() || acknowledgementStartedMs >= 0L);
    }

    private boolean animationAllowed() {
        return ValueAnimator.areAnimatorsEnabled()
                && (powerManager == null || !powerManager.isPowerSaveMode());
    }

    private boolean canBlink() {
        return homeVisible
                && getVisibility() == VISIBLE
                && mode != NowPlayingPuppetPolicy.Mode.HIDDEN
                && animationAllowed();
    }

    private void ensureBlinkScheduled() {
        if (!canBlink()
                || blinkAnimator != null
                || blinkScheduled
                || secondBlinkScheduled
                || blinksRemaining > 0) {
            return;
        }
        blinkScheduled = true;
        handler.postDelayed(blinkRunnable, NowPlayingPuppetBlink.nextDelayMillis(blinkRandom));
    }

    private void startNextBlink() {
        if (!canBlink()) {
            cancelBlink();
            return;
        }
        if (blinksRemaining <= 0) {
            ensureBlinkScheduled();
            return;
        }
        blinksRemaining--;
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        blinkAnimator = animator;
        animator.setDuration(NowPlayingPuppetBlink.DURATION_MS);
        animator.addUpdateListener(valueAnimator -> {
            float progress = (Float) valueAnimator.getAnimatedValue();
            puppet.setBlinkOpenness(NowPlayingPuppetBlink.openness(progress));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (blinkAnimator != animation) return;
                blinkAnimator = null;
                puppet.setBlinkOpenness(1f);
                if (!canBlink()) {
                    blinksRemaining = 0;
                    return;
                }
                if (blinksRemaining > 0) {
                    secondBlinkScheduled = true;
                    handler.postDelayed(secondBlinkRunnable, NowPlayingPuppetBlink.DOUBLE_GAP_MS);
                } else {
                    ensureBlinkScheduled();
                }
            }
        });
        animator.start();
    }

    private void cancelBlink() {
        handler.removeCallbacks(blinkRunnable);
        handler.removeCallbacks(secondBlinkRunnable);
        blinkScheduled = false;
        secondBlinkScheduled = false;
        blinksRemaining = 0;
        ValueAnimator animator = blinkAnimator;
        blinkAnimator = null;
        if (animator != null) animator.cancel();
        puppet.setBlinkOpenness(1f);
    }

    private void scheduleFrame() {
        if (frameScheduled || !shouldAnimateFrame()) return;
        frameScheduled = true;
        handler.postDelayed(frame, FRAME_MS);
    }

    private void stopFrames() {
        handler.removeCallbacks(frame);
        frameScheduled = false;
    }

    private void applyPose(NowPlayingPuppetMotion.Pose pose) {
        puppet.setTranslationX(pose.x);
        puppet.setTranslationY(pose.y);
        puppet.setRotation(pose.rotationDegrees);
        puppet.setScaleX(pose.scale);
        puppet.setScaleY(pose.scale);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Draws eyelids over the existing approved headphones artwork; the art itself is never regenerated. */
    private static final class BlinkingPuppetImageView extends ImageView {
        private static final float SOURCE_WIDTH = 1536f;
        private static final float SOURCE_HEIGHT = 1024f;
        private static final RectF LEFT_EYE = new RectF(395f, 465f, 711f, 790f);
        private static final RectF RIGHT_EYE = new RectF(757f, 515f, 1075f, 850f);
        private static final float TOP_LID_SHARE = 0.78f;
        private static final float BOTTOM_LID_SHARE = 0.22f;

        private final Paint eyelidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path eyeClip = new Path();
        private float blinkOpenness = 1f;

        BlinkingPuppetImageView(Context context) {
            super(context);
            // Matches the near-black approved upper eyelids/headphone shadow closely enough
            // that the existing art appears to close rather than being replaced.
            eyelidPaint.setColor(Color.rgb(8, 12, 18));
        }

        void setBlinkOpenness(float openness) {
            float next = Math.max(0f, Math.min(1f, openness));
            if (Math.abs(blinkOpenness - next) < 0.001f) return;
            blinkOpenness = next;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (blinkOpenness >= 0.999f || getWidth() <= 0 || getHeight() <= 0) return;
            Drawable drawable = getDrawable();
            if (drawable == null) return;

            float sourceAspect = SOURCE_WIDTH / SOURCE_HEIGHT;
            float viewAspect = getWidth() / (float) getHeight();
            float contentWidth;
            float contentHeight;
            if (viewAspect > sourceAspect) {
                contentHeight = getHeight();
                contentWidth = contentHeight * sourceAspect;
            } else {
                contentWidth = getWidth();
                contentHeight = contentWidth / sourceAspect;
            }
            float left = (getWidth() - contentWidth) / 2f;
            float top = (getHeight() - contentHeight) / 2f;
            float scaleX = contentWidth / SOURCE_WIDTH;
            float scaleY = contentHeight / SOURCE_HEIGHT;

            drawEyelids(canvas, LEFT_EYE, left, top, scaleX, scaleY);
            drawEyelids(canvas, RIGHT_EYE, left, top, scaleX, scaleY);
        }

        private void drawEyelids(
                Canvas canvas,
                RectF sourceEye,
                float contentLeft,
                float contentTop,
                float scaleX,
                float scaleY) {
            RectF eye = new RectF(
                    contentLeft + sourceEye.left * scaleX,
                    contentTop + sourceEye.top * scaleY,
                    contentLeft + sourceEye.right * scaleX,
                    contentTop + sourceEye.bottom * scaleY);
            float closed = 1f - blinkOpenness;
            float topCover = eye.height() * TOP_LID_SHARE * closed;
            float bottomCover = eye.height() * BOTTOM_LID_SHARE * closed;

            eyeClip.reset();
            eyeClip.addOval(eye, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(eyeClip);
            canvas.drawRect(eye.left, eye.top, eye.right, eye.top + topCover, eyelidPaint);
            canvas.drawRect(eye.left, eye.bottom - bottomCover, eye.right, eye.bottom, eyelidPaint);
            canvas.restoreToCount(save);
        }
    }
}

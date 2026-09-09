package com.boop.shieldhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    private final LayeredPuppetView puppet;
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
        setBackgroundColor(Color.TRANSPARENT);
        setFocusable(false);
        setFocusableInTouchMode(false);
        setClickable(false);
        setLongClickable(false);
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        setClipChildren(true);
        setClipToPadding(true);

        powerManager = context.getSystemService(PowerManager.class);
        puppet = new LayeredPuppetView(context);
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

    /** One poseable puppet: headphones, immutable approved eyes, then a moving top eyelid. */
    private static final class LayeredPuppetView extends FrameLayout {
        private final TopEyelidLayer eyelidLayer;

        LayeredPuppetView(Context context) {
            super(context);
            setBackgroundColor(Color.TRANSPARENT);
            setClipChildren(false);
            setClipToPadding(false);
            setFocusable(false);
            setClickable(false);
            setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);

            ImageView headphonesLayer = new ImageView(context);
            headphonesLayer.setImageResource(com.boop.shieldhome.R.drawable.boop_headphones);
            headphonesLayer.setScaleType(ImageView.ScaleType.FIT_CENTER);
            headphonesLayer.setFocusable(false);
            headphonesLayer.setClickable(false);
            headphonesLayer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(headphonesLayer, fillLayout());

            ApprovedEyesLayer eyesLayer = new ApprovedEyesLayer(context);
            eyesLayer.setFocusable(false);
            eyesLayer.setClickable(false);
            eyesLayer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(eyesLayer, fillLayout());

            eyelidLayer = new TopEyelidLayer(context);
            eyelidLayer.setFocusable(false);
            eyelidLayer.setClickable(false);
            eyelidLayer.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(eyelidLayer, fillLayout());
        }

        void setBlinkOpenness(float openness) {
            eyelidLayer.setBlinkOpenness(openness);
        }

        private FrameLayout.LayoutParams fillLayout() {
            return new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER);
        }
    }

    private static final class PuppetArtGeometry {
        static final float HEADPHONES_WIDTH = 1536f;
        static final float HEADPHONES_HEIGHT = 1024f;
        static final RectF LEFT_OLD_EYE_SLOT = new RectF(395f, 465f, 711f, 790f);
        static final RectF RIGHT_OLD_EYE_SLOT = new RectF(757f, 515f, 1075f, 850f);

        private PuppetArtGeometry() { }

        static RectF mapRect(RectF source, int width, int height) {
            return mapBox(source.left, source.top, source.right, source.bottom, width, height);
        }

        static RectF mapBox(NowPlayingPuppetEyePlacement.Box source, int width, int height) {
            return mapBox(source.left, source.top, source.right, source.bottom, width, height);
        }

        private static RectF mapBox(
                float sourceLeft,
                float sourceTop,
                float sourceRight,
                float sourceBottom,
                int width,
                int height) {
            float sourceAspect = HEADPHONES_WIDTH / HEADPHONES_HEIGHT;
            float viewAspect = width / (float) height;
            float contentWidth;
            float contentHeight;
            if (viewAspect > sourceAspect) {
                contentHeight = height;
                contentWidth = contentHeight * sourceAspect;
            } else {
                contentWidth = width;
                contentHeight = contentWidth / sourceAspect;
            }
            float left = (width - contentWidth) / 2f;
            float top = (height - contentHeight) / 2f;
            float scaleX = contentWidth / HEADPHONES_WIDTH;
            float scaleY = contentHeight / HEADPHONES_HEIGHT;
            return new RectF(
                    left + sourceLeft * scaleX,
                    top + sourceTop * scaleY,
                    left + sourceRight * scaleX,
                    top + sourceBottom * scaleY);
        }
    }

    /** Draws the exact approved pair as one undistorted layer over the legacy raster eyes. */
    private static final class ApprovedEyesLayer extends View {
        private final Bitmap approvedEyes;
        private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint oldEyeOcclusion = new Paint(Paint.ANTI_ALIAS_FLAG);

        ApprovedEyesLayer(Context context) {
            super(context);
            approvedEyes = BitmapFactory.decodeResource(
                    getResources(), com.boop.shieldhome.R.drawable.boop_approved_eyes);
            oldEyeOcclusion.setColor(Color.BLACK);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (approvedEyes == null || getWidth() <= 0 || getHeight() <= 0) return;

            // The headphones raster predates the permanent eye master and still contains old eyes.
            // Hide only those legacy eye ovals. The approved pair is then drawn once, as one unit.
            canvas.drawOval(
                    PuppetArtGeometry.mapRect(PuppetArtGeometry.LEFT_OLD_EYE_SLOT, getWidth(), getHeight()),
                    oldEyeOcclusion);
            canvas.drawOval(
                    PuppetArtGeometry.mapRect(PuppetArtGeometry.RIGHT_OLD_EYE_SLOT, getWidth(), getHeight()),
                    oldEyeOcclusion);

            RectF pair = PuppetArtGeometry.mapBox(
                    NowPlayingPuppetEyePlacement.pairInHeadphoneSource(), getWidth(), getHeight());
            canvas.drawBitmap(approvedEyes, null, pair, bitmapPaint);
        }
    }

    /** Top black round eyelid only. It is clipped to each eye, so no slab can appear above it. */
    private static final class TopEyelidLayer extends View {
        private final Paint eyelidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path eyeClip = new Path();
        private final Path lidPath = new Path();
        private float blinkOpenness = 1f;

        TopEyelidLayer(Context context) {
            super(context);
            eyelidPaint.setColor(Color.BLACK);
        }

        void setBlinkOpenness(float openness) {
            float next = Float.isFinite(openness)
                    ? Math.max(0f, Math.min(1f, openness))
                    : 1f;
            if (Math.abs(blinkOpenness - next) < 0.001f) return;
            blinkOpenness = next;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (blinkOpenness >= 0.999f || getWidth() <= 0 || getHeight() <= 0) return;
            drawTopLid(canvas, NowPlayingPuppetEyePlacement.leftEyeInHeadphoneSource());
            drawTopLid(canvas, NowPlayingPuppetEyePlacement.rightEyeInHeadphoneSource());
        }

        private void drawTopLid(Canvas canvas, NowPlayingPuppetEyePlacement.Box approvedEye) {
            RectF eye = PuppetArtGeometry.mapBox(approvedEye, getWidth(), getHeight());
            float sideY = eye.top + eye.height() * NowPlayingPuppetLidTravel.sideEdge(blinkOpenness);
            float centreY = eye.top + eye.height() * NowPlayingPuppetLidTravel.centreEdge(blinkOpenness);
            float overhang = eye.width() * 0.12f;
            float left = eye.left - overhang;
            float right = eye.right + overhang;
            float centre = eye.centerX();

            eyeClip.reset();
            eyeClip.addOval(eye, Path.Direction.CW);
            int save = canvas.save();
            canvas.clipPath(eyeClip);

            lidPath.reset();
            lidPath.moveTo(left, eye.top - eye.height());
            lidPath.lineTo(right, eye.top - eye.height());
            lidPath.lineTo(right, sideY);
            lidPath.cubicTo(
                    right - eye.width() * 0.18f, sideY,
                    centre + eye.width() * 0.18f, centreY,
                    centre, centreY);
            lidPath.cubicTo(
                    centre - eye.width() * 0.18f, centreY,
                    left + eye.width() * 0.18f, sideY,
                    left, sideY);
            lidPath.close();
            canvas.drawPath(lidPath, eyelidPaint);
            canvas.restoreToCount(save);
        }
    }
}

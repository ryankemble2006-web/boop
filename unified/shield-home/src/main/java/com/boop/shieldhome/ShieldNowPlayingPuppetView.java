package com.boop.shieldhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

/** Transparent launcher-owned headphones BOOP layer. It never participates in remote focus. */
public final class ShieldNowPlayingPuppetView extends FrameLayout {
    private static final long FRAME_MS = 33L;

    // These match ShieldHomeView's fixed HOME/nav/card geometry and ShieldNowPlayingView's
    // reserved right-hand mascot bay. The bay itself clips motion, so BOOP can never cover media UI.
    private static final int BAY_HEIGHT_DP = 154;
    private static final int BAY_RIGHT_MARGIN_DP = 60;
    private static final int BAY_TOP_MARGIN_DP = 118;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ImageView puppet;
    private final PowerManager powerManager;

    private NowPlayingSnapshot snapshot;
    private NowPlayingPuppetPolicy.Mode mode = NowPlayingPuppetPolicy.Mode.HIDDEN;
    private long grooveStartedMs;
    private long acknowledgementStartedMs = -1L;
    private boolean frameScheduled;
    private boolean homeVisible = true;

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!shouldAnimateFrame()) return;
            long now = SystemClock.uptimeMillis();
            NowPlayingPuppetMotion.Pose pose = mode == NowPlayingPuppetPolicy.Mode.GROOVE
                    ? NowPlayingPuppetMotion.groove(now - grooveStartedMs)
                    : NowPlayingPuppetMotion.rest();
            if (acknowledgementStartedMs >= 0L) {
                long ackElapsed = now - acknowledgementStartedMs;
                pose = NowPlayingPuppetMotion.acknowledge(pose, ackElapsed);
                if (ackElapsed >= NowPlayingPuppetMotion.ACK_DURATION_MS) {
                    acknowledgementStartedMs = -1L;
                }
            }
            applyPose(pose);
            if (mode == NowPlayingPuppetPolicy.Mode.GROOVE || acknowledgementStartedMs >= 0L) {
                scheduleFrame();
            }
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
        puppet = new ImageView(context);
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
        snapshot = next;
        mode = NowPlayingPuppetPolicy.mode(next);
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
                grooveStartedMs = 0L;
                applyPose(NowPlayingPuppetMotion.rest());
            }
            stopFrames();
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        long now = SystemClock.uptimeMillis();
        if (mode == NowPlayingPuppetPolicy.Mode.GROOVE && grooveStartedMs == 0L) {
            grooveStartedMs = now;
        } else if (mode != NowPlayingPuppetPolicy.Mode.GROOVE) {
            grooveStartedMs = 0L;
        }

        if (!animationAllowed()) {
            acknowledgementStartedMs = -1L;
            stopFrames();
            applyPose(NowPlayingPuppetMotion.rest());
            return;
        }

        NowPlayingPuppetMotion.Pose pose = mode == NowPlayingPuppetPolicy.Mode.GROOVE
                ? NowPlayingPuppetMotion.groove(now - grooveStartedMs)
                : NowPlayingPuppetMotion.rest();
        if (acknowledgementStartedMs >= 0L) {
            long ackElapsed = now - acknowledgementStartedMs;
            pose = NowPlayingPuppetMotion.acknowledge(pose, ackElapsed);
            if (ackElapsed >= NowPlayingPuppetMotion.ACK_DURATION_MS) {
                acknowledgementStartedMs = -1L;
            }
        }
        applyPose(pose);
        if (mode == NowPlayingPuppetPolicy.Mode.GROOVE || acknowledgementStartedMs >= 0L) {
            scheduleFrame();
        } else {
            stopFrames();
        }
    }

    @Override protected void onDetachedFromWindow() {
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

    private boolean shouldAnimateFrame() {
        return homeVisible
                && getVisibility() == VISIBLE
                && mode != NowPlayingPuppetPolicy.Mode.HIDDEN
                && animationAllowed()
                && (mode == NowPlayingPuppetPolicy.Mode.GROOVE || acknowledgementStartedMs >= 0L);
    }

    private boolean animationAllowed() {
        return ValueAnimator.areAnimatorsEnabled()
                && (powerManager == null || !powerManager.isPowerSaveMode());
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
}

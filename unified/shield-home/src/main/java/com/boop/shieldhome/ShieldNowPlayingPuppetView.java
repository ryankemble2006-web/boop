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
        setClipChildren(false);
        setClipToPadding(false);

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
        int width = Math.max(1, Math.round(w * 0.39f));
        int height = Math.max(1, Math.round(h * 0.69f));
        LayoutParams params = new LayoutParams(width, height, Gravity.END | Gravity.BOTTOM);
        params.rightMargin = dp(30);
        params.bottomMargin = dp(18);
        puppet.setLayoutParams(params);
    }

    private LayoutParams puppetLayout() {
        LayoutParams params = new LayoutParams(dp(440), dp(620), Gravity.END | Gravity.BOTTOM);
        params.rightMargin = dp(30);
        params.bottomMargin = dp(18);
        return params;
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

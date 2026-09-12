package com.boop.alpha1;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;

import com.boop.eyes.CanonicalEyeRenderer;
import com.boop.eyes.EyeCatalogue;
import com.boop.eyes.EyeMotion;
import com.boop.eyes.ProductionAnimationController;

/** Production Wall face backed by the finished canonical Animation Lab engine. */
final class BoopCanonicalFaceView extends FrameLayout {
    private static final long FRAME_MS = 33L;
    private static final String IDLE = "idle";
    private static final String WAKE = "wake";
    private static final String SLEEP = "sleep";
    private static final String LISTENING = "listening";
    private static final String THINKING = "thinking";
    private static final String BERRY_REMEMBER = "berry_remember";
    private static final String BERRY_CURIOUS = "berry_curious";
    private static final String BERRY_CHEEKY = "berry_cheeky";
    private static final String SHAKE = "shake_reaction";
    private static final String NOTIFICATION = "notification";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final GLSurfaceView surface;
    private final CanonicalEyeRenderer renderer;
    private final ProductionAnimationController animation;
    private final PowerManager powerManager;
    private boolean frameScheduled;
    private boolean animationPaused;
    private java.util.function.BooleanSupplier idleBlinkAllowed = () -> true;
    private int eyeHueDegrees = BoopEyeHueMath.DEFAULT_HUE_DEGREES;

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!shouldAnimate()) return;
            animation.setAmbientBlinkEnabled(idleBlinkAllowed == null || idleBlinkAllowed.getAsBoolean());
            renderer.pose = animation.sample(SystemClock.uptimeMillis());
            surface.requestRender();
            scheduleFrame();
        }
    };

    private final Runnable sleepHide = () -> {
        setVisibility(View.INVISIBLE);
        stopFrames();
        pauseAnimation();
    };

    BoopCanonicalFaceView(Context context) {
        super(context);
        setBackgroundColor(Color.BLACK);
        setClipChildren(false);
        setClipToPadding(false);
        powerManager = context.getSystemService(PowerManager.class);
        animation = new ProductionAnimationController(IDLE, SystemClock.uptimeMillis(), 20260910L);

        surface = new GLSurfaceView(context);
        surface.setEGLContextClientVersion(2);
        surface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surface.setZOrderOnTop(true);
        surface.setPreserveEGLContextOnPause(true);
        renderer = new CanonicalEyeRenderer(
                context.getAssets(), detail -> android.util.Log.e("BOOPEyes", detail));
        // Configure saved colour without requesting a frame before setRenderer creates GLThread.
        eyeHueDegrees = BoopEyeHueMath.clampHue(BoopEyeHue.loadHue(context));
        renderer.setHueRotationDegrees(
                BoopEyeHueMath.rotationDegreesForHue(eyeHueDegrees));
        surface.setRenderer(renderer);
        surface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        surface.setFocusable(false);
        surface.setClickable(false);
        addView(surface, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setVisibility(View.INVISIBLE);
    }

    void showIdleBlackImmediately() {
        cancelSleepHide();
        animation.setState(IDLE, SystemClock.uptimeMillis(), 0f);
        setVisibility(View.INVISIBLE);
        stopFrames();
        pauseAnimation();
    }
    void wakeFromIdle() {
        cancelSleepHide();
        long now = SystemClock.uptimeMillis();
        setVisibility(View.VISIBLE);
        resumeAnimation();
        animation.setState(IDLE, now, 0f);
        animation.trigger(WAKE, now, 0f);
        renderNow(now);
    }

    void goIdleBlack() {
        cancelSleepHide();
        long now = SystemClock.uptimeMillis();
        setVisibility(View.VISIBLE);
        resumeAnimation();
        animation.setState(IDLE, now, 0f);
        animation.trigger(SLEEP, now, 0f);
        renderNow(now);
        long duration = Math.round(EyeCatalogue.find(SLEEP).duration);
        handler.postDelayed(sleepHide, duration);
    }

    void playMemberBerry(int variant) {
        if (getVisibility() != View.VISIBLE) return;
        String id;
        switch (Math.floorMod(variant, 3)) {
            case 0: id = BERRY_REMEMBER; break;
            case 1: id = BERRY_CURIOUS; break;
            default: id = BERRY_CHEEKY; break;
        }
        trigger(id, 160f);
    }
    void playShakeMuppet(float strength) {
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE);
            resumeAnimation();
        }
        trigger(SHAKE, 0f);
    }

    void startThinking() {
        setSteady(THINKING, 160f);
    }

    void stopThinking() {
        if (THINKING.equals(animation.steadyClipId())) {
            setSteady(IDLE, 160f);
        }
    }

    void setListening(boolean active) {
        if (active) {
            setSteady(LISTENING, 120f);
        } else if (LISTENING.equals(animation.steadyClipId())) {
            setSteady(IDLE, 120f);
        }
    }

    void startListeningCue() { setListening(true); }
    void stopListeningCue() { setListening(false); }
    void setIdleBlinkAllowed(java.util.function.BooleanSupplier allowed) {
        idleBlinkAllowed = allowed == null ? () -> true : allowed;
    }
    void setEyeHueDegrees(int hueDegrees) {
        eyeHueDegrees = BoopEyeHueMath.clampHue(hueDegrees);
        renderer.setHueRotationDegrees(BoopEyeHueMath.rotationDegreesForHue(eyeHueDegrees));
        surface.requestRender();
    }

    void playCanonicalClip(String id) {
        EyeMotion.Clip clip = EyeCatalogue.find(id);
        if (clip.loop) setSteady(clip.id, 120f);
        else trigger(clip.id, 120f);
    }

    void playNotification() {
        cancelSleepHide();
        long now = SystemClock.uptimeMillis();
        setVisibility(View.VISIBLE);
        resumeAnimation();
        animation.setState(IDLE, now, 0f);
        animation.trigger(NOTIFICATION, now, 120f);
        renderNow(now);
    }
    private void setSteady(String id, float blendMs) {
        cancelSleepHide();
        long now = SystemClock.uptimeMillis();
        setVisibility(View.VISIBLE);
        resumeAnimation();
        animation.setState(id, now, blendMs);
        renderNow(now);
    }

    private void trigger(String id, float blendMs) {
        cancelSleepHide();
        long now = SystemClock.uptimeMillis();
        resumeAnimation();
        animation.trigger(id, now, blendMs);
        renderNow(now);
    }

    private void renderNow(long now) {
        if (!animationAllowed()) {
            EyeMotion.Clip steady = EyeCatalogue.find(animation.steadyClipId());
            renderer.pose = steady.sample(steady.loop ? 0 : steady.duration);
            surface.requestRender();
            stopFrames();
            return;
        }
        renderer.pose = animation.sample(now);
        surface.requestRender();
        scheduleFrame();
    }

    private boolean animationAllowed() {
        return ValueAnimator.areAnimatorsEnabled()
                && (powerManager == null || !powerManager.isPowerSaveMode());
    }
    private boolean shouldAnimate() {
        return isAttachedToWindow()
                && getVisibility() == View.VISIBLE
                && animationAllowed();
    }

    private void scheduleFrame() {
        if (frameScheduled || !shouldAnimate()) return;
        frameScheduled = true;
        handler.postDelayed(frame, FRAME_MS);
    }

    private void stopFrames() {
        handler.removeCallbacks(frame);
        frameScheduled = false;
    }

    private void cancelSleepHide() {
        handler.removeCallbacks(sleepHide);
    }

    private void pauseAnimation() {
        if (animationPaused) return;
        animation.pause(SystemClock.uptimeMillis());
        animationPaused = true;
    }

    private void resumeAnimation() {
        if (!animationPaused) return;
        animation.resume(SystemClock.uptimeMillis());
        animationPaused = false;
    }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        surface.onResume();
        resumeAnimation();
        if (getVisibility() == View.VISIBLE) scheduleFrame();
    }

    @Override protected void onDetachedFromWindow() {
        cancelSleepHide();
        stopFrames();
        pauseAnimation();
        surface.onPause();
        super.onDetachedFromWindow();
    }
}

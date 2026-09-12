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
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
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
    private final com.boop.eyes.ProductionAnimationController animation =
            new com.boop.eyes.ProductionAnimationController("idle", SystemClock.uptimeMillis(), 20260910L);

    private NowPlayingSnapshot snapshot;
    private NowPlayingPuppetPolicy.Mode mode = NowPlayingPuppetPolicy.Mode.HIDDEN;
    private boolean frameScheduled;
    private boolean homeVisible = true;
    private boolean animationPaused;
    private com.boop.shared.BoopState.Owner presentationOwner = com.boop.shared.BoopState.Owner.HOME_NOW_PLAYING;
    private Runnable unsubscribeShared;

    public void setPresentationOwner(com.boop.shared.BoopState.Owner owner) {
        presentationOwner = owner;
        applyCurrentState();
    }

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!shouldAnimateFrame()) return;
            puppet.setCanonicalPose(animation.sample(SystemClock.uptimeMillis()));
            scheduleFrame();
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
        unsubscribeShared = com.boop.shared.BoopState.INSTANCE.subscribe(ignored -> applyCurrentState());
    }

    public void setSnapshot(NowPlayingSnapshot next) {
        NowPlayingSnapshot previous = snapshot;
        snapshot = next;
        mode = NowPlayingPuppetPolicy.mode(next);
        long now = SystemClock.uptimeMillis();
        String steady = CanonicalMediaAnimationPolicy.steadyClip(mode);
        if (!steady.equals(animation.steadyClipId())) {
            animation.setState(steady, now, 160f);
        }
        boolean changed = previous != null && next != null
                && (previous.sessionId() != next.sessionId()
                    || !previous.trackKey().equals(next.trackKey()));
        if (changed && mode != NowPlayingPuppetPolicy.Mode.HIDDEN) {
            animation.trigger(CanonicalMediaAnimationPolicy.trackChangeClip(), now, 120f);
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
        boolean visible = homeVisible
                && mode != NowPlayingPuppetPolicy.Mode.HIDDEN
                && com.boop.shared.BoopState.INSTANCE.snapshot().owner == presentationOwner;
        if (!visible) {
            pauseCanonicalAnimation();
            stopFrames();
            setVisibility(GONE);
            return;
        }

        setVisibility(VISIBLE);
        resumeCanonicalAnimation();
        long now = SystemClock.uptimeMillis();
        if (!animationAllowed()) {
            stopFrames();
            com.boop.eyes.EyeMotion.Clip steady =
                    com.boop.eyes.EyeCatalogue.find(animation.steadyClipId());
            puppet.setCanonicalPose(steady.sample(steady.loop ? 0 : steady.duration));
            return;
        }
        puppet.setCanonicalPose(animation.sample(now));
        scheduleFrame();
    }

    private void pauseCanonicalAnimation() {
        if (animationPaused) return;
        animation.pause(SystemClock.uptimeMillis());
        animationPaused = true;
    }

    private void resumeCanonicalAnimation() {
        if (!animationPaused) return;
        animation.resume(SystemClock.uptimeMillis());
        animationPaused = false;
    }

    @Override protected void onDetachedFromWindow() {
        if(unsubscribeShared != null) { unsubscribeShared.run(); unsubscribeShared=null; }
        pauseCanonicalAnimation();
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
                && animationAllowed();
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

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Headphones remain approved artwork; the eye surface is the Animation Lab renderer. */
    private static final class LayeredPuppetView extends FrameLayout {
        private final GLSurfaceView eyeSurface;
        private final com.boop.eyes.CanonicalEyeRenderer eyeRenderer;

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

            eyeSurface = new GLSurfaceView(context);
            eyeSurface.setEGLContextClientVersion(2);
            eyeSurface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            eyeSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            eyeSurface.setZOrderOnTop(true);
            eyeSurface.setPreserveEGLContextOnPause(true);
            eyeRenderer = new com.boop.eyes.CanonicalEyeRenderer(
                    context.getAssets(), detail -> android.util.Log.e("BOOPEyes", detail));
            eyeSurface.setRenderer(eyeRenderer);
            eyeSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            eyeSurface.setFocusable(false);
            eyeSurface.setClickable(false);
            addView(eyeSurface, new FrameLayout.LayoutParams(1, 1));
        }

        void setCanonicalPose(com.boop.eyes.EyeMotion.Pose pose) {
            eyeRenderer.pose = pose;
            eyeSurface.requestRender();
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            eyeSurface.onResume();
        }

        @Override protected void onDetachedFromWindow() {
            eyeSurface.onPause();
            super.onDetachedFromWindow();
        }

        @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w <= 0 || h <= 0) return;
            RectF pair = PuppetArtGeometry.mapBox(
                    NowPlayingPuppetEyePlacement.pairInHeadphoneSource(), w, h);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    Math.max(1, Math.round(pair.width())),
                    Math.max(1, Math.round(pair.height())));
            params.leftMargin = Math.round(pair.left);
            params.topMargin = Math.round(pair.top);
            eyeSurface.setLayoutParams(params);
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

}

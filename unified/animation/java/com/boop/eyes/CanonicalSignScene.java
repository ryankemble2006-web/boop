package com.boop.eyes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.FrameLayout;

/** The accepted Lab performance: one eye surface and one sign, sharing one clock. */
public final class CanonicalSignScene extends FrameLayout {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final GLSurfaceView eyes;
    private final CanonicalEyeRenderer renderer;
    private final NotificationSignView sign;
    private final PuppetClock clock = new PuppetClock(0);
    private int style;
    private boolean attached, scheduled;
    private Runnable unwatch;
    private final Runnable frame = () -> { scheduled=false; drawFrame(); };
    public CanonicalSignScene(Context context) {
        super(context); setBackgroundColor(Color.BLACK);
        eyes = new GLSurfaceView(context);
        eyes.setEGLContextClientVersion(2);
        eyes.setEGLConfigChooser(8,8,8,8,16,0);
        eyes.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        eyes.setZOrderMediaOverlay(true);
        eyes.setPreserveEGLContextOnPause(true);
        renderer = new CanonicalEyeRenderer(context.getAssets(), detail -> android.util.Log.e("BOOPEyes",detail));
        eyes.setRenderer(renderer);
        eyes.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        eyes.setFocusable(false); eyes.setClickable(false);
        addView(eyes, new LayoutParams(1,1));
        sign = new NotificationSignView(context);
        addView(sign, new LayoutParams(1,1));
    }
    public void identity(int variant,String label,Drawable icon) {
        style=Math.floorMod(variant,4); sign.setIdentity(label,icon); drawFrame();
    }
    public void setEyesVisible(boolean visible) { eyes.setVisibility(visible?VISIBLE:INVISIBLE); }
    private void drawFrame() {
        if(!attached || !isShown() || getWindowVisibility()!=VISIBLE)return;
        long now=SystemClock.uptimeMillis();
        clock.setSpeed(PuppetPreferences.speed(getContext()),now);
        SignMotion.Pose pose=SignMotion.sample(clock.now(now),style);
        renderer.setHueRotationDegrees(PuppetPreferences.hue(getContext())-190);
        renderer.pose=pose.eyes; sign.show(pose,style); eyes.requestRender();
        if(!scheduled && PuppetPreferences.speed(getContext())>0) {
            scheduled=true; handler.postDelayed(frame,16);
        }
    }
    private void stopFrames() { handler.removeCallbacks(frame); scheduled=false; }
    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow(); attached=true; eyes.onResume();
        clock.resume(SystemClock.uptimeMillis());
        unwatch=PuppetPreferences.watch(getContext(),this::drawFrame); drawFrame();
    }
    @Override protected void onDetachedFromWindow() {
        attached=false; stopFrames(); clock.pause(SystemClock.uptimeMillis());
        if(unwatch!=null) { unwatch.run(); unwatch=null; }
        eyes.onPause(); super.onDetachedFromWindow();
    }
    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if(clock==null || eyes==null)return;
        if(visibility!=VISIBLE) { stopFrames(); clock.pause(SystemClock.uptimeMillis()); }
        else { clock.resume(SystemClock.uptimeMillis()); drawFrame(); }
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        super.onSizeChanged(w,h,oldw,oldh);
        if(w<=0 || h<=0)return;
        int sceneHeight=Math.min(h,Math.round(w*.68f));
        int sceneWidth=Math.min(w,Math.round(sceneHeight/.68f));
        int left=(w-sceneWidth)/2, top=(h-sceneHeight)/2;
        LayoutParams ep=new LayoutParams(sceneWidth,Math.max(1,Math.round(sceneHeight*.64f)));
        ep.leftMargin=left;ep.topMargin=top;eyes.setLayoutParams(ep);
        LayoutParams sp=new LayoutParams(sceneWidth,sceneHeight);
        sp.leftMargin=left;sp.topMargin=top;sign.setLayoutParams(sp);
    }
}

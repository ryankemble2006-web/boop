package com.boop.alpha1;

import android.app.Activity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.SeekBar;

final class BoopEyeHueOverlay {
    private final Activity activity;
    private final FrameLayout surface;
    private final BoopFaceView face;
    private final SeekBar slider;
    private boolean visible;

    BoopEyeHueOverlay(Activity activity, FrameLayout surface, BoopFaceView face) {
        this.activity = activity;
        this.surface = surface;
        this.face = face;
        slider = new SeekBar(activity);
        slider.setMax(BoopEyeHueMath.PROGRESS_MAX);
        slider.setProgress(BoopEyeHue.loadHue(activity));
        slider.setContentDescription("Eye colour hue");
        slider.setVisibility(android.view.View.GONE);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                BoopEyeHue.saveHue(activity, progress);
                face.setEyeHueDegrees(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        surface.addView(slider, sliderLayoutParams());
    }

    boolean isVisible() {
        return visible;
    }

    void show() {
        slider.setProgress(BoopEyeHue.loadHue(activity));
        FrameLayout.LayoutParams params = sliderLayoutParams();
        slider.setLayoutParams(params);
        slider.setVisibility(android.view.View.VISIBLE);
        slider.bringToFront();
        visible = true;
    }

    void hide() {
        slider.setVisibility(android.view.View.GONE);
        visible = false;
    }

    boolean isSliderTouch(MotionEvent event) {
        if (!visible || event == null) return false;
        float x = event.getX();
        float y = event.getY();
        return x >= slider.getLeft() && x <= slider.getRight()
                && y >= slider.getTop() && y <= slider.getBottom();
    }

    static boolean touchesBothEyes(BoopFaceView face, MotionEvent event) {
        if (face == null || event == null || event.getPointerCount() < 2) return false;
        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(face.getWidth(), face.getHeight());
        if (!layout.landscape()) return false;
        boolean left = false;
        boolean right = false;
        for (int index = 0; index < event.getPointerCount(); index++) {
            float x = event.getX(index);
            float y = event.getY(index);
            left |= inside(layout.left(), x, y);
            right |= inside(layout.right(), x, y);
        }
        return left && right;
    }

    private static boolean inside(BoopEyeLayout.Eye eye, float x, float y) {
        if (eye == null) return false;
        float halfWidth = eye.width() / 2f;
        float halfHeight = eye.height() / 2f;
        return x >= eye.centerX() - halfWidth && x <= eye.centerX() + halfWidth
                && y >= eye.centerY() - halfHeight && y <= eye.centerY() + halfHeight;
    }

    private FrameLayout.LayoutParams sliderLayoutParams() {
        int width = Math.max(dp(280), Math.round(face.getWidth() * 0.56f));
        width = Math.min(width, Math.max(dp(280), face.getWidth() - dp(64)));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, dp(64));
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

        BoopEyeLayout.Layout layout = BoopEyeLayout.calculate(face.getWidth(), face.getHeight());
        int top;
        if (layout.landscape()) {
            float eyeBottom = layout.left().centerY() + (layout.left().height() / 2f);
            top = Math.round(eyeBottom) + dp(20);
        } else {
            top = Math.round(face.getHeight() * 0.68f);
        }
        params.topMargin = Math.max(dp(16), Math.min(top, Math.max(dp(16), face.getHeight() - dp(80))));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}

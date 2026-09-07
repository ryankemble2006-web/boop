package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

final class BoopEyeHueSettings {
    private BoopEyeHueSettings() { }

    static void addSlider(Activity activity, LinearLayout overlay, BoopFaceView face) {
        if (activity == null || overlay == null || face == null) {
            return;
        }

        TextView label = new TextView(activity);
        label.setText("Eye colour");
        label.setTextColor(Color.WHITE);
        label.setTextSize(22f);
        label.setGravity(Gravity.CENTER);
        overlay.addView(label);

        SeekBar slider = new SeekBar(activity);
        slider.setMax(BoopEyeHueMath.PROGRESS_MAX);
        slider.setProgress(BoopEyeHue.loadHue(activity));
        slider.setContentDescription("Eye colour hue");
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                BoopEyeHue.saveHue(activity, progress);
                face.setEyeHueDegrees(progress);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(activity, 64));
        params.setMargins(0, dp(activity, 4), 0, dp(activity, 28));
        overlay.addView(slider, params);
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}

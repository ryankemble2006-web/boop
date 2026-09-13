package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.boop.eyes.AnimationSpeedPreferences;

/** Shared settings without constructing an additional face or animation renderer. */
public final class BoopAppearanceActivity extends Activity {
    private static final double[] SPEEDS = {.5, 1, 1.5, 2};
    private static final String[] SPEED_LABELS = {"0.5x", "1x", "1.5x", "2x"};
    private BoopSharedEyeColourRuntime sharing;
    private SharedPreferences eyes, appearance;
    private TextView status, hueLabel, speedLabel;
    private SeekBar hue;
    private Button share, retry;
    private final Button[] speedButtons = new Button[4];
    private Runnable unwatch;
    private boolean dragging;
    private final SharedPreferences.OnSharedPreferenceChangeListener hueListener = (store, key) -> {
        if (key == null || "hue_degrees".equals(key)) refresh();
    };
    private final SharedPreferences.OnSharedPreferenceChangeListener speedListener = (store, key) -> {
        if (key == null || "animation_speed".equals(key)) refreshSpeed();
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        sharing = BoopSharedEyeColourRuntime.get(this);
        eyes = getSharedPreferences("boop_eyes", MODE_PRIVATE);
        appearance = getSharedPreferences("boop_appearance", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(28), dp(24), dp(28), dp(24));
        column.setBackgroundColor(Color.BLACK);
        scroll.addView(column);
        text(column, "Eyes and animation", 28);
        text(column, "Only the iris colour changes. BOOP's artwork stays the same.", 18);
        hueLabel = text(column, "Eye colour", 20);
        hue = new SeekBar(this);
        hue.setMax(359);
        hue.setKeyProgressIncrement(5);
        hue.setContentDescription("Eye colour hue");
        hue.setProgress(BoopEyeHue.loadHue(this));
        hue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                if (fromUser) BoopEyeHue.saveHue(BoopAppearanceActivity.this, value);
                hueLabel.setText("Eye colour: " + value);
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { dragging = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) { dragging = false; refresh(); }
        });
        column.addView(hue, new LinearLayout.LayoutParams(-1, dp(60)));
        button(column, "Original blue", () -> BoopEyeHue.saveHue(this, BoopEyeHueMath.DEFAULT_HUE_DEGREES));
        text(column, "Share the same eye colour with your other BOOPs using the same Home Assistant. Sharing works while BOOP is open; the last colour stays available offline.", 18);
        status = text(column, "", 18);
        share = button(column, "Share eye colour: Off", () -> {
            if (sharing.enabled()) sharing.setEnabled(false);
            else confirmSharing();
        });
        retry = button(column, "Retry sharing", this::confirmSharing);
        speedLabel = text(column, "Animation speed: 1x", 20);
        text(column, "BOOP only, on this device. 1x keeps the original timing. Android transitions, music and voice stay unchanged.", 18);
        LinearLayout speeds = new LinearLayout(this);
        speeds.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < SPEEDS.length; i++) {
            final int index = i;
            Button button = new Button(this);
            button.setText(SPEED_LABELS[i]); button.setTextSize(18); button.setAllCaps(false);
            button.setMinWidth(0); button.setMinimumWidth(0); button.setMinHeight(dp(60));
            button.setOnClickListener(v -> AnimationSpeedPreferences.save(this, SPEEDS[index]));
            button.setOnFocusChangeListener((v, focused) -> refreshSpeed());
            speedButtons[i] = button;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(64), 1f);
            params.setMargins(dp(3), dp(4), dp(3), dp(4));
            speeds.addView(button, params);
        }
        column.addView(speeds, new LinearLayout.LayoutParams(-1, -2));
        button(column, "Done", this::finish);
        setContentView(scroll);
        refresh();
        refreshSpeed();
    }
    private void confirmSharing() {
        new AlertDialog.Builder(this).setTitle("Share BOOP's eye colour?")
                .setMessage("BOOP will use one eye-colour setting in your paired Home Assistant. If needed, it will create that setting using your administrator access. Existing shared colour is used first. Offline edits stay local and are not sent later.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Share", (dialog, which) -> sharing.setEnabled(true)).show();
    }
    private void refresh() {
        if (hue == null) return;
        int value = BoopEyeHue.loadHue(this);
        if (!dragging) hue.setProgress(value);
        hueLabel.setText("Eye colour: " + value);
        share.setText("Share eye colour: " + (sharing.enabled() ? "On" : "Off"));
        status.setText(sharing.enabled() ? sharing.status() : "Eye colour stays on this device.");
        retry.setVisibility(sharing.enabled() && !sharing.ready()
                ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void refreshSpeed() {
        if (speedLabel == null) return;
        double selected = AnimationSpeedPreferences.load(this);
        for (int i = 0; i < SPEEDS.length; i++) {
            Button button = speedButtons[i];
            if (button == null) continue;
            boolean chosen = SPEEDS[i] == selected;
            if (chosen) speedLabel.setText("Animation speed: " + SPEED_LABELS[i]);
            button.setSelected(chosen);
            button.setTextColor(chosen || button.hasFocus() ? 0xff4db8ff : Color.WHITE);
            button.setContentDescription("BOOP animation speed " + SPEED_LABELS[i] + (chosen ? ", selected" : ""));
            GradientDrawable background = new GradientDrawable();
            background.setColor(Color.rgb(42, 42, 42));
            background.setCornerRadius(dp(10));
            if (button.hasFocus() || chosen) background.setStroke(dp(button.hasFocus() ? 3 : 1), 0xff4db8ff);
            button.setBackground(background);
        }
    }
    @Override protected void onStart() {
        super.onStart();
        eyes.registerOnSharedPreferenceChangeListener(hueListener);
        appearance.registerOnSharedPreferenceChangeListener(speedListener);
        unwatch = sharing.observe(this::refresh);
        refreshSpeed();
    }
    @Override protected void onStop() {
        eyes.unregisterOnSharedPreferenceChangeListener(hueListener);
        appearance.unregisterOnSharedPreferenceChangeListener(speedListener);
        if (unwatch != null) { unwatch.run(); unwatch = null; }
        super.onStop();
    }
    private TextView text(LinearLayout column, String value, int size) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(Color.WHITE);
        view.setPadding(0, dp(10), 0, dp(10));
        column.addView(view);
        return view;
    }
    private Button button(LinearLayout column, String title, Runnable action) {
        Button view = new Button(this);
        view.setText(title); view.setTextSize(20); view.setMinHeight(dp(60));
        view.setOnClickListener(v -> action.run());
        column.addView(view);
        return view;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}

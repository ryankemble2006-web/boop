package com.boop.alpha1;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/** Shared settings without constructing an additional face or animation renderer. */
public final class BoopAppearanceActivity extends Activity {
    private BoopSharedEyeColourRuntime sharing;
    private TextView status, hueLabel;
    private SeekBar hue;
    private Button share, retry;
    private Runnable unwatch;
    private boolean dragging;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        sharing = BoopSharedEyeColourRuntime.get(this);
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
        button(column, "Done", this::finish);
        setContentView(scroll);
        refresh();
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
    @Override protected void onStart() { super.onStart(); unwatch = sharing.observe(this::refresh); }
    @Override protected void onStop() {
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

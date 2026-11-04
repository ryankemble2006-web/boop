package com.boop.shieldoverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class CrashReportActivity extends Activity {
    static final String EXTRA_REPORT = "com.boop.shieldoverlay.extra.CRASH_REPORT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String report = getIntent() == null ? null : getIntent().getStringExtra(EXTRA_REPORT);
        if (report == null || report.trim().isEmpty()) {
            report = "No crash details were saved.";
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setFillViewport(true);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(48), dp(40), dp(48), dp(40));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = text("BOOP launch crash", 38f, Color.WHITE, true);
        content.addView(title);

        TextView intro = text(
                "Photograph this screen or send me the first exception and the first BOOP line.",
                22f,
                Color.LTGRAY,
                false);
        LinearLayout.LayoutParams introParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        introParams.topMargin = dp(12);
        introParams.bottomMargin = dp(22);
        content.addView(intro, introParams);

        TextView details = text(report, 18f, Color.WHITE, false);
        details.setTypeface(Typeface.MONOSPACE);
        details.setTextIsSelectable(true);
        content.addView(details);

        Button retry = new Button(this);
        retry.setText("Try BOOP again");
        retry.setTextSize(24f);
        retry.setAllCaps(false);
        retry.setFocusable(true);
        retry.setMinHeight(dp(68));
        retry.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(28);
        retryParams.bottomMargin = dp(20);
        content.addView(retry, retryParams);
        retry.requestFocus();

        setContentView(scroll);
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}

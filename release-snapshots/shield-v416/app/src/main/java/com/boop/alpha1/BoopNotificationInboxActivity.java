package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BoopNotificationInboxActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        final BoopNotificationRuntime runtime;
        try {
            runtime = BoopNotificationRuntime.get(this);
        } catch (RuntimeException unavailable) {
            finish();
            return;
        }

        List<BoopNotificationEnvelope> active = runtime.activeNotifications();
        if (active.isEmpty()) {
            finish();
            return;
        }

        LinkedHashMap<String, List<BoopNotificationEnvelope>> grouped = new LinkedHashMap<>();
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();
        for (BoopNotificationEnvelope item : active) {
            if (item == null || item.key().isEmpty()) continue;
            String packageName = item.packageName();
            grouped.computeIfAbsent(packageName, ignored -> new ArrayList<>()).add(item);
            String label = item.appLabel();
            labels.put(packageName, label == null || label.trim().isEmpty() ? packageName : label);
        }
        if (grouped.isEmpty()) {
            finish();
            return;
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setFillViewport(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        column.setPadding(pad, dp(28), pad, dp(36));
        scroll.addView(column, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView eyebrow = new TextView(this);
        eyebrow.setText("ACTIVE NOTIFICATIONS");
        eyebrow.setTextSize(11f);
        eyebrow.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        eyebrow.setLetterSpacing(0.08f);
        eyebrow.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
        column.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText("Notifications");
        title.setTextSize(28f);
        title.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        title.setLetterSpacing(-0.015f);
        title.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(2);
        titleLp.bottomMargin = dp(16);
        column.addView(title, titleLp);

        for (Map.Entry<String, List<BoopNotificationEnvelope>> group : grouped.entrySet()) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(com.boop.shieldhome.BoopTvChrome.sectionPanel(this));
            card.setPadding(dp(18), dp(16), dp(18), dp(16));
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
            cardLp.bottomMargin = dp(14);

            TextView app = new TextView(this);
            app.setText(labels.get(group.getKey()));
            app.setTextSize(13f);
            app.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
            app.setLetterSpacing(0.04f);
            app.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
            LinearLayout.LayoutParams appLp = new LinearLayout.LayoutParams(-1, -2);
            appLp.bottomMargin = dp(8);
            card.addView(app, appLp);

            for (BoopNotificationEnvelope item : group.getValue()) {
                Button itemBtn = new Button(this);
                itemBtn.setAllCaps(false);
                itemBtn.setText(cardText(item));
                itemBtn.setTextSize(15.5f);
                itemBtn.setTextColor(Color.WHITE);
                itemBtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                itemBtn.setMinHeight(dp(48));
                com.boop.shieldhome.BoopTvChrome.styleInteractive(itemBtn);
                itemBtn.setContentDescription("Open notification from " + labels.get(group.getKey()));
                itemBtn.setOnClickListener(v -> open(runtime, item.key()));
                LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLp.bottomMargin = dp(6);
                card.addView(itemBtn, itemLp);
            }
            column.addView(card, cardLp);
        }

        Button close = new Button(this);
        close.setAllCaps(false);
        close.setText("Close");
        close.setTextSize(16f);
        close.setTextColor(Color.WHITE);
        close.setMinHeight(dp(52));
        com.boop.shieldhome.BoopTvChrome.styleInteractive(close);
        close.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        closeParams.topMargin = dp(12);
        column.addView(close, closeParams);

        setContentView(scroll);
    }

    private void open(BoopNotificationRuntime runtime, String key) {
        BoopNotificationTapLauncher.Result result = runtime.openNotification(this, key);
        if (result == BoopNotificationTapLauncher.Result.OPENED) {
            finish();
            return;
        }
        Toast.makeText(this, "Can't open that right now.", Toast.LENGTH_SHORT).show();
        render();
    }

    private String cardText(BoopNotificationEnvelope item) {
        String title = item.title();
        String text = item.text();
        boolean hasTitle = title != null && !title.trim().isEmpty();
        boolean hasText = text != null && !text.trim().isEmpty();
        if (hasTitle && hasText) return title + "\n" + text;
        if (hasTitle) return title;
        if (hasText) return text;
        String channel = item.channelName();
        return channel == null || channel.trim().isEmpty() ? "Notification" : channel;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
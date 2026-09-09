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
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        column.setPadding(pad, pad, pad, pad);
        scroll.addView(column, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = label("BOOP notifications", 26f, true);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        column.addView(title, fullWidthWrap());

        for (Map.Entry<String, List<BoopNotificationEnvelope>> group : grouped.entrySet()) {
            TextView app = label(labels.get(group.getKey()), 21f, true);
            LinearLayout.LayoutParams appParams = fullWidthWrap();
            appParams.setMargins(0, dp(18), 0, dp(6));
            column.addView(app, appParams);

            for (BoopNotificationEnvelope item : group.getValue()) {
                Button card = new Button(this);
                card.setAllCaps(false);
                card.setText(cardText(item));
                card.setTextSize(17f);
                card.setTextColor(Color.WHITE);
                card.setBackgroundColor(Color.rgb(36, 36, 36));
                card.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                card.setPadding(dp(16), dp(12), dp(16), dp(12));
                card.setContentDescription("Open notification from " + labels.get(group.getKey()));
                card.setOnClickListener(v -> open(runtime, item.key()));
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.setMargins(0, dp(4), 0, dp(4));
                column.addView(card, cardParams);
            }
        }

        Button close = new Button(this);
        close.setAllCaps(false);
        close.setText("Close");
        close.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(56));
        closeParams.setMargins(0, dp(20), 0, 0);
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

    private TextView label(String value, float sizeSp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(sizeSp);
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private static LinearLayout.LayoutParams fullWidthWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

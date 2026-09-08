package com.boop.alpha1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BoopNotificationSettingsActivity extends Activity {
    private static final String NO_CATEGORIES =
            "No notification categories seen yet. When this app sends one, BOOP will learn the category here. It will not interrupt until you enable that category.";
    private static final String ANDROID_STILL_ALERTING =
            "Android is still alerting for this category. Make it silent there before BOOP uses his own sound.";

    private BoopNotificationSettingsStore store;
    private BoopNotificationSettingsState state;
    private LinearLayout content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new BoopNotificationSettingsStore(this);
        state = store.load();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(28), dp(30), dp(28), dp(36));
        scroll.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (store != null && content != null) {
            state = store.load();
            render();
        }
    }

    private void render() {
        content.removeAllViews();
        addText("Notifications", 30f, true, 0);
        addText(readiness(), 18f, false, 12);

        Switch master = switchRow("BOOP Notifications", state.masterEnabled());
        master.setContentDescription("BOOP Notifications master switch");
        master.setOnCheckedChangeListener((button, checked) ->
                persist(state.withMasterEnabled(checked), false));
        addWithBottom(master, 22);

        TextView timeoutLabel = addText(timeoutLabel(), 19f, false, 4);
        SeekBar timeout = new SeekBar(this);
        timeout.setMax(27);
        timeout.setProgress((int) (state.timeoutMs() / 1000L) - 3);
        timeout.setContentDescription("Notification display time");
        timeout.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                long seconds = 3L + progress;
                persist(state.withTimeoutMs(seconds * 1000L), false);
                timeoutLabel.setText(timeoutLabel());
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        addWithBottom(timeout, 26);

        Set<BoopNotificationChannelInfo> observedSet = observedChannels();
        Map<String, List<BoopNotificationChannelInfo>> channelsByPackage = groupChannels(observedSet);
        List<BoopNotificationAppEntry> apps = BoopNotificationAppCatalog.load(this, observedSet);

        addText("Apps", 23f, true, 12);
        for (BoopNotificationAppEntry app : apps) {
            if (app.packageName().equals(getPackageName())) continue;
            addApp(app, channelsByPackage.get(app.packageName()));
        }

        Button done = button("Done");
        done.setContentDescription("Close notification settings");
        done.setOnClickListener(v -> finish());
        addWithBottom(done, 0);
    }

    private void addApp(BoopNotificationAppEntry app, List<BoopNotificationChannelInfo> channels) {
        Switch appSwitch = switchRow(app.label(), state.isAppEnabled(app.packageName()));
        appSwitch.setContentDescription("Allow BOOP notifications from " + app.label());
        appSwitch.setOnCheckedChangeListener((button, checked) -> {
            Set<String> apps = new LinkedHashSet<>(state.enabledApps());
            if (checked) apps.add(app.packageName()); else apps.remove(app.packageName());
            persist(state.withEnabledApps(apps), true);
        });
        content.addView(appSwitch, matchWrap());
        addText(app.packageName(), 13f, false, 7);

        if (!state.isAppEnabled(app.packageName())) {
            addSpacer(10);
            return;
        }

        if (channels == null || channels.isEmpty()) {
            addText(NO_CATEGORIES, 15f, false, 18);
            return;
        }

        for (BoopNotificationChannelInfo channel : channels) {
            addChannel(channel);
        }
        addSpacer(12);
    }

    private void addChannel(BoopNotificationChannelInfo channel) {
        String channelName = channel.channelName().trim().isEmpty()
                ? channel.channelId() : channel.channelName();
        Switch channelSwitch = switchRow(
                channelName,
                state.isChannelEnabled(channel.packageName(), channel.channelId()));
        channelSwitch.setContentDescription("Allow BOOP notification category " + channelName);
        channelSwitch.setOnCheckedChangeListener((button, checked) -> {
            Set<String> channels = new LinkedHashSet<>(state.enabledChannelKeys());
            String key = BoopNotificationSettingsCodec.channelKey(
                    channel.packageName(), channel.channelId());
            if (checked) channels.add(key); else channels.remove(key);
            persist(state.withEnabledChannelKeys(channels), false);
        });
        LinearLayout.LayoutParams switchParams = matchWrap();
        switchParams.setMargins(dp(20), 0, 0, 0);
        content.addView(channelSwitch, switchParams);

        String nativeStatus;
        if (!channel.effectsKnown()) {
            nativeStatus = "Android alert status is not known yet. BOOP will not add his own sound.";
        } else if (channel.nativeEffectsSilent()) {
            nativeStatus = "Android alerts are silent. BOOP may use his own sound.";
        } else {
            nativeStatus = ANDROID_STILL_ALERTING;
        }
        TextView status = text(nativeStatus, 14f, false);
        LinearLayout.LayoutParams statusParams = matchWrap();
        statusParams.setMargins(dp(28), 0, 0, dp(5));
        content.addView(status, statusParams);

        Button androidSettings = button("Open Android channel settings");
        androidSettings.setTextSize(14f);
        androidSettings.setContentDescription("Open Android settings for " + channelName);
        androidSettings.setOnClickListener(v -> openAndroidChannelSettings(channel));
        LinearLayout.LayoutParams buttonParams = matchWrap();
        buttonParams.setMargins(dp(24), 0, 0, dp(10));
        content.addView(androidSettings, buttonParams);
    }

    private void openAndroidChannelSettings(BoopNotificationChannelInfo channel) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, channel.packageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, channel.channelId());
        try {
            startActivity(intent);
        } catch (RuntimeException unavailable) {
            Toast.makeText(this, "Android can't open that category right now.", Toast.LENGTH_SHORT).show();
        }
    }

    private Set<BoopNotificationChannelInfo> observedChannels() {
        try {
            return BoopNotificationRuntime.get(this).observedChannels();
        } catch (RuntimeException unavailable) {
            return store.observedChannels();
        }
    }

    private Map<String, List<BoopNotificationChannelInfo>> groupChannels(
            Set<BoopNotificationChannelInfo> observed) {
        Map<String, List<BoopNotificationChannelInfo>> result = new LinkedHashMap<>();
        for (BoopNotificationChannelInfo channel : observed) {
            result.computeIfAbsent(channel.packageName(), ignored -> new ArrayList<>()).add(channel);
        }
        Comparator<BoopNotificationChannelInfo> comparator = Comparator
                .comparing((BoopNotificationChannelInfo item) ->
                        item.channelName().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.channelId().toLowerCase(Locale.ROOT));
        for (List<BoopNotificationChannelInfo> channels : result.values()) {
            channels.sort(comparator);
        }
        return result;
    }

    private void persist(BoopNotificationSettingsState updated, boolean rerender) {
        state = updated;
        store.save(state);
        try {
            BoopNotificationRuntime.get(this).refreshSettings();
        } catch (RuntimeException ignored) {
            // Saved settings remain authoritative and will be loaded next runtime start.
        }
        if (rerender) render();
    }

    private String readiness() {
        boolean listener = BoopNotificationPermissionState.hasListenerAccess(this);
        boolean overlay = BoopNotificationPermissionState.hasOverlayAccess(this);
        if (listener && overlay) return "Ready";
        if (!listener && !overlay) return "Needs Notification Access and Display Permission";
        if (!listener) return "Needs Notification Access";
        return "Needs Display Permission";
    }

    private String timeoutLabel() {
        return "Display for " + (state.timeoutMs() / 1000L) + " seconds";
    }

    private Switch switchRow(String label, boolean checked) {
        Switch control = new Switch(this);
        control.setText(label);
        control.setTextColor(Color.WHITE);
        control.setTextSize(18f);
        control.setGravity(Gravity.CENTER_VERTICAL);
        control.setChecked(checked);
        control.setPadding(0, dp(8), 0, dp(8));
        return control;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(42, 42, 42));
        return button;
    }

    private TextView addText(String value, float sizeSp, boolean bold, int bottomDp) {
        TextView view = text(value, sizeSp, bold);
        addWithBottom(view, bottomDp);
        return view;
    }

    private TextView text(String value, float sizeSp, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(sizeSp);
        if (bold) view.setTypeface(view.getTypeface(), android.graphics.Typeface.BOLD);
        return view;
    }

    private void addWithBottom(View view, int bottomDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.setMargins(0, 0, 0, dp(bottomDp));
        content.addView(view, params);
    }

    private void addSpacer(int heightDp) {
        View spacer = new View(this);
        content.addView(spacer, new LinearLayout.LayoutParams(1, dp(heightDp)));
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

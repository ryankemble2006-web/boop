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
    private static final String STATE_PENDING_MASTER_ENABLE = "pending_master_enable";

    private BoopNotificationSettingsStore store;
    private BoopNotificationSettingsState state;
    private LinearLayout content;
    private boolean pendingMasterEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new BoopNotificationSettingsStore(this);
        state = store.load();
        if (savedInstanceState != null) {
            pendingMasterEnable = savedInstanceState.getBoolean(
                    STATE_PENDING_MASTER_ENABLE, false);
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.BLACK);
        scroll.setFillViewport(true);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(24), dp(28), dp(24), dp(36));
        scroll.addView(content, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        render();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_PENDING_MASTER_ENABLE, pendingMasterEnable);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (store != null && content != null) {
            state = store.load();
            if (pendingMasterEnable) {
                boolean granted = BoopNotificationPermissionState.hasListenerAccess(this);
                pendingMasterEnable = false;
                if (granted) {
                    persist(state.withMasterEnabled(true), false);
                }
            }
            render();
        }
    }

    private void render() {
        content.removeAllViews();

        TextView eyebrow = new TextView(this);
        eyebrow.setText("PHONE & WALL INTEGRATION");
        eyebrow.setTextSize(11f);
        eyebrow.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        eyebrow.setLetterSpacing(0.08f);
        eyebrow.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
        content.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText("Notifications & Overlay");
        title.setTextSize(28f);
        title.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        title.setLetterSpacing(-0.015f);
        title.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(2);
        titleLp.bottomMargin = dp(4);
        content.addView(title, titleLp);

        TextView status = new TextView(this);
        status.setText(readiness());
        status.setTextSize(14f);
        status.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_REGULAR);
        status.setTextColor(com.boop.shieldhome.BoopTvChrome.TEXT_SECONDARY);
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(-1, -2);
        statusLp.bottomMargin = dp(16);
        content.addView(status, statusLp);

        // Permissions card
        if (!BoopNotificationPermissionState.hasListenerAccess(this) || !BoopNotificationPermissionState.hasOverlayAccess(this)) {
            LinearLayout permCard = sectionCard("SYSTEM PERMISSIONS REQUIRED");
            if (!BoopNotificationPermissionState.hasListenerAccess(this)) {
                Button access = button("Grant Notification Access");
                access.setOnClickListener(v -> launchNotificationAccess());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
                lp.bottomMargin = dp(8);
                permCard.addView(access, lp);
            }
            if (!BoopNotificationPermissionState.hasOverlayAccess(this)) {
                Button display = button("Grant Display Over Other Apps");
                display.setOnClickListener(v -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:" + getPackageName())));
                    } catch (RuntimeException unavailable) {
                        Toast.makeText(this, "Open Android settings to allow display over other apps.", Toast.LENGTH_LONG).show();
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
                permCard.addView(display, lp);
            }
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(-1, -2);
            cardLp.bottomMargin = dp(16);
            content.addView(permCard, cardLp);
        }

        // Master controls card
        LinearLayout masterCard = sectionCard("NOTIFICATION DELIVERY");
        Switch master = switchRow("Enable BOOP Notifications", state.masterEnabled());
        master.setContentDescription("BOOP Notifications master switch");
        master.setOnCheckedChangeListener((button, checked) -> {
            BoopNotificationMasterToggle.Action action = BoopNotificationMasterToggle.action(
                    checked,
                    BoopNotificationPermissionState.hasListenerAccess(this));
            if (action == BoopNotificationMasterToggle.Action.REQUEST_LISTENER_ACCESS) {
                pendingMasterEnable = true;
                button.setChecked(false);
                launchNotificationAccess();
                return;
            }
            pendingMasterEnable = false;
            persist(state.withMasterEnabled(action == BoopNotificationMasterToggle.Action.ENABLE_NOW), false);
        });
        LinearLayout.LayoutParams masterLp = new LinearLayout.LayoutParams(-1, -2);
        masterLp.bottomMargin = dp(12);
        masterCard.addView(master, masterLp);

        Switch allApps = switchRow("Include all apps automatically", state.allAppsEnabled());
        allApps.setOnCheckedChangeListener((button, checked) -> persist(state.withAllAppsEnabled(checked), true));
        masterCard.addView(allApps, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout.LayoutParams mcLp = new LinearLayout.LayoutParams(-1, -2);
        mcLp.bottomMargin = dp(16);
        content.addView(masterCard, mcLp);

        // Display timer card
        LinearLayout timerCard = sectionCard("DISPLAY DURATION");
        TextView timeoutLabel = new TextView(this);
        timeoutLabel.setText(timeoutLabel());
        timeoutLabel.setTextSize(15f);
        timeoutLabel.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_REGULAR);
        timeoutLabel.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tlLp = new LinearLayout.LayoutParams(-1, -2);
        tlLp.bottomMargin = dp(10);
        timerCard.addView(timeoutLabel, tlLp);

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
        timerCard.addView(timeout, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(-1, -2);
        tcLp.bottomMargin = dp(16);
        content.addView(timerCard, tcLp);

        // Apps card
        Set<BoopNotificationChannelInfo> observedSet = observedChannels();
        Map<String, List<BoopNotificationChannelInfo>> channelsByPackage = groupChannels(observedSet);
        List<BoopNotificationAppEntry> apps = BoopNotificationAppCatalog.load(this, observedSet);

        LinearLayout appsCard = sectionCard("APP SELECTION (" + apps.size() + " APPS)");
        for (BoopNotificationAppEntry app : apps) {
            if (app.packageName().equals(getPackageName())) continue;
            addApp(appsCard, app, channelsByPackage.get(app.packageName()));
        }
        LinearLayout.LayoutParams acLp = new LinearLayout.LayoutParams(-1, -2);
        acLp.bottomMargin = dp(20);
        content.addView(appsCard, acLp);

        Button done = button("Done");
        done.setContentDescription("Close notification settings");
        done.setMinHeight(dp(52));
        done.setOnClickListener(v -> finish());
        content.addView(done, new LinearLayout.LayoutParams(-1, -2));
    }

    private LinearLayout sectionCard(String eyebrowText) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(com.boop.shieldhome.BoopTvChrome.sectionPanel(this));
        TextView header = new TextView(this);
        header.setText(eyebrowText);
        header.setTextSize(11f);
        header.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        header.setLetterSpacing(0.08f);
        header.setTextColor(com.boop.shieldhome.BoopTvChrome.accentColor(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.bottomMargin = dp(12);
        card.addView(header, lp);
        return card;
    }

    private void launchNotificationAccess() {
        try {
            startActivity(BoopNotificationPermissionState.notificationListenerSettingsIntent());
        } catch (RuntimeException unavailable) {
            pendingMasterEnable = false;
            Toast.makeText(this, "Android can't open Notification Access right now.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addApp(LinearLayout container, BoopNotificationAppEntry app, List<BoopNotificationChannelInfo> channels) {
        Switch appSwitch = switchRow(app.label(), state.isAppEnabled(app.packageName()));
        appSwitch.setContentDescription("Allow BOOP notifications from " + app.label());
        appSwitch.setOnCheckedChangeListener((button, checked) -> {
            persist(state.withAppEnabled(app.packageName(), checked), true);
        });
        LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(-1, -2);
        swLp.bottomMargin = dp(8);
        container.addView(appSwitch, swLp);

        if (!state.isAppEnabled(app.packageName())) {
            return;
        }

        if (channels != null && !channels.isEmpty()) {
            for (BoopNotificationChannelInfo channel : channels) {
                addChannel(container, channel);
            }
        }
    }

    private void addChannel(LinearLayout container, BoopNotificationChannelInfo channel) {
        String channelName = channel.channelName().trim().isEmpty()
                ? channel.channelId() : channel.channelName();
        Switch channelSwitch = switchRow(channelName,
                state.isChannelEnabled(channel.packageName(), channel.channelId()));
        channelSwitch.setTextSize(14.5f);
        channelSwitch.setContentDescription("Allow BOOP notification category " + channelName);
        channelSwitch.setOnCheckedChangeListener((button, checked) -> {
            persist(state.withChannelEnabled(channel.packageName(), channel.channelId(), checked), false);
        });
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(-1, -2);
        switchParams.setMargins(dp(24), 0, 0, dp(6));
        container.addView(channelSwitch, switchParams);
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
        } catch (RuntimeException ignored) { }
        if (rerender) render();
    }

    private String readiness() {
        boolean listener = BoopNotificationPermissionState.hasListenerAccess(this);
        boolean overlay = BoopNotificationPermissionState.hasOverlayAccess(this);
        if (listener && overlay) return "✓ All permissions granted and ready.";
        if (!listener && !overlay) return "Needs Notification Access and Display Permission.";
        if (!listener) return "Needs Notification Access.";
        return "Needs Display Permission.";
    }

    private String timeoutLabel() {
        return "Display for " + (state.timeoutMs() / 1000L) + " seconds";
    }

    private Switch switchRow(String label, boolean checked) {
        Switch control = new Switch(this);
        control.setText(label);
        control.setTextColor(Color.WHITE);
        control.setTextSize(16f);
        control.setTypeface(com.boop.shieldhome.BoopTvChrome.FONT_MEDIUM);
        control.setGravity(Gravity.CENTER_VERTICAL);
        control.setChecked(checked);
        control.setPadding(dp(12), dp(8), dp(12), dp(8));
        com.boop.shieldhome.BoopTvChrome.styleInteractive(control);
        return control;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15f);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setMinHeight(dp(48));
        com.boop.shieldhome.BoopTvChrome.styleInteractive(button);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
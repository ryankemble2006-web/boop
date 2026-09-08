package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class BoopNotificationSettingsStore {
    private static final String PREFS_NAME = "boop_notifications";
    private static final String KEY_MASTER_ENABLED = "master_enabled";
    private static final String KEY_TIMEOUT_MS = "timeout_ms";
    private static final String KEY_ENABLED_APPS = "enabled_apps";
    private static final String KEY_ENABLED_CHANNELS = "enabled_channels";
    private static final String KEY_OBSERVED_CHANNELS = "observed_channels";

    private final SharedPreferences preferences;

    BoopNotificationSettingsStore(Context context) {
        if (context == null) throw new IllegalArgumentException("context required");
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    BoopNotificationSettingsState load() {
        return new BoopNotificationSettingsState(
                preferences.getBoolean(KEY_MASTER_ENABLED, false),
                preferences.getLong(KEY_TIMEOUT_MS, BoopNotificationSettingsState.DEFAULT_TIMEOUT_MS),
                copy(preferences.getStringSet(KEY_ENABLED_APPS, Collections.emptySet())),
                copy(preferences.getStringSet(KEY_ENABLED_CHANNELS, Collections.emptySet())));
    }

    void save(BoopNotificationSettingsState state) {
        BoopNotificationSettingsState safe = state == null
                ? BoopNotificationSettingsState.defaults() : state;
        preferences.edit()
                .putBoolean(KEY_MASTER_ENABLED, safe.masterEnabled())
                .putLong(KEY_TIMEOUT_MS, safe.timeoutMs())
                .putStringSet(KEY_ENABLED_APPS, new LinkedHashSet<>(safe.enabledApps()))
                .putStringSet(KEY_ENABLED_CHANNELS, new LinkedHashSet<>(safe.enabledChannelKeys()))
                .apply();
    }

    Set<BoopNotificationChannelInfo> observedChannels() {
        Set<BoopNotificationChannelInfo> result = new LinkedHashSet<>();
        for (String encoded : preferences.getStringSet(KEY_OBSERVED_CHANNELS, Collections.emptySet())) {
            try {
                result.add(BoopNotificationChannelInfo.decode(encoded));
            } catch (IllegalArgumentException ignored) {
                // Ignore a corrupt local record rather than widening notification access.
            }
        }
        return Collections.unmodifiableSet(result);
    }

    void recordObservedChannel(BoopNotificationChannelInfo channel) {
        if (channel == null || channel.packageName().isEmpty() || channel.channelId().isEmpty()) {
            return;
        }
        Set<BoopNotificationChannelInfo> existing = new LinkedHashSet<>(observedChannels());
        existing.removeIf(item -> item.key().equals(channel.key()));
        existing.add(channel);
        Set<String> encoded = new LinkedHashSet<>();
        for (BoopNotificationChannelInfo item : existing) encoded.add(item.encode());
        preferences.edit().putStringSet(KEY_OBSERVED_CHANNELS, encoded).apply();
    }

    private static Set<String> copy(Set<String> values) {
        return values == null ? Collections.emptySet() : new LinkedHashSet<>(values);
    }
}

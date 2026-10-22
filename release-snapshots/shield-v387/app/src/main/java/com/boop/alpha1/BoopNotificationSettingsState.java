package com.boop.alpha1;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

final class BoopNotificationSettingsState {
    static final long DEFAULT_TIMEOUT_MS = 8_000L;
    static final long MIN_TIMEOUT_MS = 3_000L;
    static final long MAX_TIMEOUT_MS = 30_000L;

    private final boolean masterEnabled;
    private final long timeoutMs;
    private final Set<String> enabledApps;
    private final Set<String> enabledChannelKeys;
    private final boolean allAppsEnabled;
    private final Set<String> excludedApps, excludedChannelKeys;

    BoopNotificationSettingsState(
            boolean masterEnabled,
            long timeoutMs,
            Set<String> enabledApps,
            Set<String> enabledChannelKeys) {
        this(masterEnabled, timeoutMs, enabledApps, enabledChannelKeys, false, null, null);
    }

    BoopNotificationSettingsState(boolean masterEnabled, long timeoutMs,
            Set<String> enabledApps, Set<String> enabledChannelKeys, boolean allAppsEnabled,
            Set<String> excludedApps, Set<String> excludedChannelKeys) {
        this.masterEnabled = masterEnabled;
        this.timeoutMs = clampTimeout(timeoutMs);
        this.enabledApps = immutableCopy(enabledApps);
        this.enabledChannelKeys = immutableCopy(enabledChannelKeys);
        this.allAppsEnabled = allAppsEnabled;
        this.excludedApps = immutableCopy(excludedApps);
        this.excludedChannelKeys = immutableCopy(excludedChannelKeys);
    }

    static BoopNotificationSettingsState defaults() {
        return new BoopNotificationSettingsState(
                false, DEFAULT_TIMEOUT_MS, Collections.emptySet(), Collections.emptySet());
    }

    boolean masterEnabled() { return masterEnabled; }
    boolean allAppsEnabled() { return allAppsEnabled; }
    Set<String> excludedApps() { return excludedApps; }
    Set<String> excludedChannelKeys() { return excludedChannelKeys; }
    BoopNotificationSettingsState withAllAppsEnabled(boolean enabled) {
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, enabledApps,
                enabledChannelKeys, enabled, excludedApps, excludedChannelKeys);
    }
    BoopNotificationSettingsState withAppEnabled(String app, boolean enabled) {
        if (allAppsEnabled) return new BoopNotificationSettingsState(masterEnabled, timeoutMs,
                enabledApps, enabledChannelKeys, true, changed(excludedApps, app, !enabled), excludedChannelKeys);
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, changed(enabledApps, app, enabled),
                enabledChannelKeys, false, changed(excludedApps, app, !enabled), excludedChannelKeys);
    }
    BoopNotificationSettingsState withChannelEnabled(String app, String channel, boolean enabled) {
        String key = BoopNotificationSettingsCodec.channelKey(app, channel);
        if (allAppsEnabled) return new BoopNotificationSettingsState(masterEnabled, timeoutMs,
                enabledApps, enabledChannelKeys, true, excludedApps, changed(excludedChannelKeys, key, !enabled));
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, enabledApps,
                changed(enabledChannelKeys, key, enabled), false, excludedApps, changed(excludedChannelKeys, key, !enabled));
    }
    private static Set<String> changed(Set<String> original, String value, boolean enabled) {
        Set<String> result = new LinkedHashSet<>(original);
        if (enabled) result.add(value); else result.remove(value);
        return result;
    }
    long timeoutMs() { return timeoutMs; }
    Set<String> enabledApps() { return enabledApps; }
    Set<String> enabledChannelKeys() { return enabledChannelKeys; }

    boolean isAppEnabled(String packageName) {
        return packageName != null && !packageName.isEmpty()
                && !excludedApps.contains(packageName) && (allAppsEnabled || enabledApps.contains(packageName));
    }

    boolean isChannelEnabled(String packageName, String channelId) {
        if (packageName == null || packageName.isEmpty() || channelId == null || channelId.isEmpty()) {
            return false;
        }
        String key = BoopNotificationSettingsCodec.channelKey(packageName, channelId);
        return !excludedChannelKeys.contains(key) && (allAppsEnabled || enabledChannelKeys.contains(key));
    }

    BoopNotificationSettingsState withMasterEnabled(boolean enabled) {
        return new BoopNotificationSettingsState(enabled, timeoutMs, enabledApps, enabledChannelKeys,
                allAppsEnabled, excludedApps, excludedChannelKeys);
    }

    BoopNotificationSettingsState withTimeoutMs(long timeout) {
        return new BoopNotificationSettingsState(masterEnabled, timeout, enabledApps, enabledChannelKeys,
                allAppsEnabled, excludedApps, excludedChannelKeys);
    }

    BoopNotificationSettingsState withEnabledApps(Set<String> apps) {
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, apps, enabledChannelKeys,
                allAppsEnabled, excludedApps, excludedChannelKeys);
    }

    BoopNotificationSettingsState withEnabledChannelKeys(Set<String> channels) {
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, enabledApps, channels,
                allAppsEnabled, excludedApps, excludedChannelKeys);
    }

    private static Set<String> immutableCopy(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    private static long clampTimeout(long value) {
        return Math.max(MIN_TIMEOUT_MS, Math.min(MAX_TIMEOUT_MS, value));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoopNotificationSettingsState)) return false;
        BoopNotificationSettingsState that = (BoopNotificationSettingsState) other;
        return masterEnabled == that.masterEnabled
                && timeoutMs == that.timeoutMs
                && enabledApps.equals(that.enabledApps)
                && enabledChannelKeys.equals(that.enabledChannelKeys)
                && allAppsEnabled == that.allAppsEnabled
                && excludedApps.equals(that.excludedApps)
                && excludedChannelKeys.equals(that.excludedChannelKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterEnabled, timeoutMs, enabledApps, enabledChannelKeys,
                allAppsEnabled, excludedApps, excludedChannelKeys);
    }
}

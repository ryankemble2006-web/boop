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

    BoopNotificationSettingsState(
            boolean masterEnabled,
            long timeoutMs,
            Set<String> enabledApps,
            Set<String> enabledChannelKeys) {
        this.masterEnabled = masterEnabled;
        this.timeoutMs = clampTimeout(timeoutMs);
        this.enabledApps = immutableCopy(enabledApps);
        this.enabledChannelKeys = immutableCopy(enabledChannelKeys);
    }

    static BoopNotificationSettingsState defaults() {
        return new BoopNotificationSettingsState(
                false, DEFAULT_TIMEOUT_MS, Collections.emptySet(), Collections.emptySet());
    }

    boolean masterEnabled() { return masterEnabled; }
    long timeoutMs() { return timeoutMs; }
    Set<String> enabledApps() { return enabledApps; }
    Set<String> enabledChannelKeys() { return enabledChannelKeys; }

    boolean isAppEnabled(String packageName) {
        return packageName != null && enabledApps.contains(packageName);
    }

    boolean isChannelEnabled(String packageName, String channelId) {
        if (packageName == null || channelId == null) {
            return false;
        }
        return enabledChannelKeys.contains(
                BoopNotificationSettingsCodec.channelKey(packageName, channelId));
    }

    BoopNotificationSettingsState withMasterEnabled(boolean enabled) {
        return new BoopNotificationSettingsState(enabled, timeoutMs, enabledApps, enabledChannelKeys);
    }

    BoopNotificationSettingsState withTimeoutMs(long timeout) {
        return new BoopNotificationSettingsState(masterEnabled, timeout, enabledApps, enabledChannelKeys);
    }

    BoopNotificationSettingsState withEnabledApps(Set<String> apps) {
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, apps, enabledChannelKeys);
    }

    BoopNotificationSettingsState withEnabledChannelKeys(Set<String> channels) {
        return new BoopNotificationSettingsState(masterEnabled, timeoutMs, enabledApps, channels);
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
                && enabledChannelKeys.equals(that.enabledChannelKeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(masterEnabled, timeoutMs, enabledApps, enabledChannelKeys);
    }
}

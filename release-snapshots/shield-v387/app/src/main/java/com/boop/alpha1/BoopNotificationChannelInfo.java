package com.boop.alpha1;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

final class BoopNotificationChannelInfo {
    private final String packageName;
    private final String channelId;
    private final String channelName;
    private final boolean effectsKnown;
    private final boolean nativeSoundEnabled;
    private final boolean nativeVibrationEnabled;
    private final long lastSeenMs;

    BoopNotificationChannelInfo(
            String packageName,
            String channelId,
            String channelName,
            boolean effectsKnown,
            boolean nativeSoundEnabled,
            boolean nativeVibrationEnabled,
            long lastSeenMs) {
        this.packageName = safe(packageName);
        this.channelId = safe(channelId);
        this.channelName = safe(channelName);
        this.effectsKnown = effectsKnown;
        this.nativeSoundEnabled = nativeSoundEnabled;
        this.nativeVibrationEnabled = nativeVibrationEnabled;
        this.lastSeenMs = lastSeenMs;
    }

    String packageName() { return packageName; }
    String channelId() { return channelId; }
    String channelName() { return channelName; }
    boolean effectsKnown() { return effectsKnown; }
    boolean nativeSoundEnabled() { return nativeSoundEnabled; }
    boolean nativeVibrationEnabled() { return nativeVibrationEnabled; }
    long lastSeenMs() { return lastSeenMs; }

    boolean nativeEffectsSilent() {
        return effectsKnown && !nativeSoundEnabled && !nativeVibrationEnabled;
    }

    String key() {
        return BoopNotificationSettingsCodec.channelKey(packageName, channelId);
    }

    String encode() {
        return encodePart(packageName) + "."
                + encodePart(channelId) + "."
                + encodePart(channelName) + "."
                + (effectsKnown ? "1" : "0") + "."
                + (nativeSoundEnabled ? "1" : "0") + "."
                + (nativeVibrationEnabled ? "1" : "0") + "."
                + lastSeenMs;
    }

    static BoopNotificationChannelInfo decode(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("encoded channel is null");
        }
        String[] parts = encoded.split("\\.", -1);
        if (parts.length != 7) {
            throw new IllegalArgumentException("invalid encoded channel");
        }
        try {
            return new BoopNotificationChannelInfo(
                    decodePart(parts[0]),
                    decodePart(parts[1]),
                    decodePart(parts[2]),
                    "1".equals(parts[3]),
                    "1".equals(parts[4]),
                    "1".equals(parts[5]),
                    Long.parseLong(parts[6]));
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException("invalid encoded channel", failure);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String encodePart(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(safe(value).getBytes(StandardCharsets.UTF_8));
    }

    private static String decodePart(String value) {
        if (value.isEmpty()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoopNotificationChannelInfo)) return false;
        BoopNotificationChannelInfo that = (BoopNotificationChannelInfo) other;
        return effectsKnown == that.effectsKnown
                && nativeSoundEnabled == that.nativeSoundEnabled
                && nativeVibrationEnabled == that.nativeVibrationEnabled
                && lastSeenMs == that.lastSeenMs
                && packageName.equals(that.packageName)
                && channelId.equals(that.channelId)
                && channelName.equals(that.channelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, channelId, channelName, effectsKnown,
                nativeSoundEnabled, nativeVibrationEnabled, lastSeenMs);
    }
}

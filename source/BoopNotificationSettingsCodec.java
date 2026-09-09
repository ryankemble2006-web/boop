package com.boop.alpha1;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;

final class BoopNotificationSettingsCodec {
    private BoopNotificationSettingsCodec() { }

    static String channelKey(String packageName, String channelId) {
        return encodePart(packageName) + "." + encodePart(channelId);
    }

    static String encodeState(BoopNotificationSettingsState state) {
        BoopNotificationSettingsState safe = state == null
                ? BoopNotificationSettingsState.defaults() : state;
        return (safe.masterEnabled() ? "1" : "0") + "|"
                + safe.timeoutMs() + "|"
                + joinEncoded(safe.enabledApps()) + "|"
                + String.join(",", safe.enabledChannelKeys());
    }

    static BoopNotificationSettingsState decodeState(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return BoopNotificationSettingsState.defaults();
        }
        String[] parts = encoded.split("\\|", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("invalid notification settings");
        }
        try {
            return new BoopNotificationSettingsState(
                    "1".equals(parts[0]),
                    Long.parseLong(parts[1]),
                    decodeSet(parts[2]),
                    rawSet(parts[3]));
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException("invalid notification settings", failure);
        }
    }

    private static String joinEncoded(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String value : values) {
            if (out.length() > 0) out.append(',');
            out.append(encodePart(value));
        }
        return out.toString();
    }

    private static Set<String> decodeSet(String encoded) {
        Set<String> values = new LinkedHashSet<>();
        if (encoded == null || encoded.isEmpty()) return values;
        for (String part : encoded.split(",", -1)) {
            if (!part.isEmpty()) values.add(decodePart(part));
        }
        return values;
    }

    private static Set<String> rawSet(String encoded) {
        Set<String> values = new LinkedHashSet<>();
        if (encoded == null || encoded.isEmpty()) return values;
        for (String part : encoded.split(",", -1)) {
            if (!part.isEmpty()) values.add(part);
        }
        return values;
    }

    private static String encodePart(String value) {
        String safe = value == null ? "" : value;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(safe.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodePart(String value) {
        if (value == null || value.isEmpty()) return "";
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}

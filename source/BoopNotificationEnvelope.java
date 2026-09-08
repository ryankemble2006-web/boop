package com.boop.alpha1;

import java.util.Objects;

final class BoopNotificationEnvelope {
    private final String key;
    private final String packageName;
    private final String appLabel;
    private final String channelId;
    private final String channelName;
    private final String title;
    private final String text;
    private final long postTimeMs;
    private final boolean autoCancel;

    BoopNotificationEnvelope(
            String key,
            String packageName,
            String appLabel,
            String channelId,
            String channelName,
            String title,
            String text,
            long postTimeMs,
            boolean autoCancel) {
        this.key = safe(key);
        this.packageName = safe(packageName);
        this.appLabel = safe(appLabel);
        this.channelId = safe(channelId);
        this.channelName = safe(channelName);
        this.title = title;
        this.text = text;
        this.postTimeMs = postTimeMs;
        this.autoCancel = autoCancel;
    }

    String key() { return key; }
    String packageName() { return packageName; }
    String appLabel() { return appLabel; }
    String channelId() { return channelId; }
    String channelName() { return channelName; }
    String title() { return title; }
    String text() { return text; }
    long postTimeMs() { return postTimeMs; }
    boolean autoCancel() { return autoCancel; }

    BoopNotificationEnvelope redactedForLockScreen() {
        return new BoopNotificationEnvelope(
                key, packageName, appLabel, channelId, channelName,
                null, null, postTimeMs, autoCancel);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoopNotificationEnvelope)) return false;
        BoopNotificationEnvelope that = (BoopNotificationEnvelope) other;
        return postTimeMs == that.postTimeMs
                && autoCancel == that.autoCancel
                && key.equals(that.key)
                && packageName.equals(that.packageName)
                && appLabel.equals(that.appLabel)
                && channelId.equals(that.channelId)
                && channelName.equals(that.channelName)
                && Objects.equals(title, that.title)
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, packageName, appLabel, channelId, channelName,
                title, text, postTimeMs, autoCancel);
    }
}

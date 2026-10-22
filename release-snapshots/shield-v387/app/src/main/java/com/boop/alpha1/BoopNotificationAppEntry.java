package com.boop.alpha1;

import java.util.Objects;

final class BoopNotificationAppEntry {
    private final String packageName;
    private final String label;

    BoopNotificationAppEntry(String packageName, String label) {
        this.packageName = safe(packageName);
        String safeLabel = safe(label).trim();
        this.label = safeLabel.isEmpty() ? this.packageName : safeLabel;
    }

    String packageName() { return packageName; }
    String label() { return label; }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoopNotificationAppEntry)) return false;
        BoopNotificationAppEntry that = (BoopNotificationAppEntry) other;
        return packageName.equals(that.packageName) && label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, label);
    }
}

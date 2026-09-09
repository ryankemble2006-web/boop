package com.boop.alpha1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class BoopNotificationCoordinator {
    enum Kind {
        IGNORE,
        PRESENT,
        UPDATE
    }

    static final class Decision {
        private final Kind kind;
        private final boolean playCue;
        private final List<BoopNotificationEnvelope> bundle;

        private Decision(Kind kind, boolean playCue, List<BoopNotificationEnvelope> bundle) {
            this.kind = kind;
            this.playCue = playCue;
            this.bundle = Collections.unmodifiableList(new ArrayList<>(bundle));
        }

        static Decision ignore() {
            return new Decision(Kind.IGNORE, false, Collections.emptyList());
        }

        Kind kind() { return kind; }
        boolean playCue() { return playCue; }
        List<BoopNotificationEnvelope> bundle() { return bundle; }
    }

    private final long burstWindowMs;
    private final LinkedHashMap<String, BoopNotificationEnvelope> active = new LinkedHashMap<>();
    private final LinkedHashSet<String> visibleKeys = new LinkedHashSet<>();
    private long visibleStartedAtMs = Long.MIN_VALUE;

    BoopNotificationCoordinator(long burstWindowMs) {
        this.burstWindowMs = Math.max(0L, burstWindowMs);
    }

    synchronized Decision onPosted(
            BoopNotificationEnvelope envelope,
            long nowMs,
            BoopNotificationSettingsState settings) {
        if (envelope == null
                || envelope.key().isEmpty()
                || !BoopNotificationPolicy.allows(
                        settings, envelope.packageName(), envelope.channelId())) {
            return Decision.ignore();
        }

        active.put(envelope.key(), envelope);

        if (visibleKeys.contains(envelope.key())) {
            return new Decision(Kind.UPDATE, false, visibleBundleLocked());
        }

        if (visibleKeys.isEmpty()) {
            visibleKeys.add(envelope.key());
            visibleStartedAtMs = nowMs;
            return new Decision(Kind.PRESENT, true, visibleBundleLocked());
        }

        if (visibleKeys.size() == 1) {
            long elapsed = nowMs - visibleStartedAtMs;
            if (elapsed >= 0L && elapsed <= burstWindowMs) {
                visibleKeys.add(envelope.key());
                return new Decision(Kind.UPDATE, false, visibleBundleLocked());
            }
            visibleKeys.clear();
            visibleKeys.add(envelope.key());
            visibleStartedAtMs = nowMs;
            return new Decision(Kind.PRESENT, true, visibleBundleLocked());
        }

        visibleKeys.add(envelope.key());
        return new Decision(Kind.UPDATE, false, visibleBundleLocked());
    }

    synchronized void rebuild(
            Collection<BoopNotificationEnvelope> activeNotifications,
            BoopNotificationSettingsState settings) {
        active.clear();
        visibleKeys.clear();
        visibleStartedAtMs = Long.MIN_VALUE;
        if (activeNotifications == null) return;
        for (BoopNotificationEnvelope envelope : activeNotifications) {
            if (envelope == null || envelope.key().isEmpty()) continue;
            if (BoopNotificationPolicy.allows(
                    settings, envelope.packageName(), envelope.channelId())) {
                active.put(envelope.key(), envelope);
            }
        }
    }

    synchronized void onRemoved(String key) {
        if (key == null) return;
        active.remove(key);
        visibleKeys.remove(key);
        if (visibleKeys.isEmpty()) {
            visibleStartedAtMs = Long.MIN_VALUE;
        }
    }

    synchronized void onPresentationDismissed() {
        visibleKeys.clear();
        visibleStartedAtMs = Long.MIN_VALUE;
    }

    synchronized List<BoopNotificationEnvelope> activeNotifications() {
        return Collections.unmodifiableList(new ArrayList<>(active.values()));
    }

    synchronized List<BoopNotificationEnvelope> visibleBundle() {
        return Collections.unmodifiableList(visibleBundleLocked());
    }

    private List<BoopNotificationEnvelope> visibleBundleLocked() {
        List<BoopNotificationEnvelope> result = new ArrayList<>();
        for (String key : visibleKeys) {
            BoopNotificationEnvelope item = active.get(key);
            if (item != null) result.add(item);
        }
        return result;
    }
}

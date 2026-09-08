package com.boop.alpha1;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class BoopNotificationRuntime {
    static final long BURST_WINDOW_MS = 4_000L;

    static final class RuntimeRecord {
        private final BoopNotificationEnvelope envelope;
        private final PendingIntent contentIntent;
        private final BoopNotificationChannelInfo channelInfo;

        RuntimeRecord(
                BoopNotificationEnvelope envelope,
                PendingIntent contentIntent,
                BoopNotificationChannelInfo channelInfo) {
            if (envelope == null) {
                throw new IllegalArgumentException("envelope required");
            }
            this.envelope = envelope;
            this.contentIntent = contentIntent;
            this.channelInfo = channelInfo;
        }

        BoopNotificationEnvelope envelope() { return envelope; }
        PendingIntent contentIntent() { return contentIntent; }
        BoopNotificationChannelInfo channelInfo() { return channelInfo; }
    }

    private static volatile BoopNotificationRuntime instance;

    private final Application application;
    private final BoopNotificationSettingsStore settingsStore;
    private final BoopNotificationCoordinator coordinator;
    private final LinkedHashMap<String, RuntimeRecord> records = new LinkedHashMap<>();

    private BoopNotificationSettingsState settings;
    private BoopNotificationListenerService attachedListener;

    private BoopNotificationRuntime(Application application) {
        this.application = application;
        this.settingsStore = new BoopNotificationSettingsStore(application);
        this.settings = settingsStore.load();
        this.coordinator = new BoopNotificationCoordinator(BURST_WINDOW_MS);
    }

    static boolean shouldInitializeForMode(BoopDeviceProfile.Mode mode) {
        return mode == BoopDeviceProfile.Mode.WALL || mode == BoopDeviceProfile.Mode.LAUNCHER;
    }

    static synchronized void initialize(Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application required");
        }
        if (!shouldInitializeForMode(BoopDeviceProfile.resolve(application))) {
            return;
        }
        if (instance == null) {
            instance = new BoopNotificationRuntime(application);
        }
    }

    static BoopNotificationRuntime get(Context context) {
        BoopNotificationRuntime current = instance;
        if (current != null) {
            return current;
        }
        if (context == null) {
            throw new IllegalStateException("notification runtime not initialized");
        }
        Context appContext = context.getApplicationContext();
        if (!(appContext instanceof Application)) {
            throw new IllegalStateException("application context unavailable");
        }
        initialize((Application) appContext);
        current = instance;
        if (current == null) {
            throw new IllegalStateException("notification runtime unavailable for this device mode");
        }
        return current;
    }

    synchronized BoopNotificationSettingsState settings() {
        return settings;
    }

    synchronized void refreshSettings() {
        settings = settingsStore.load();
        if (attachedListener != null) {
            attachedListener.rebuildActiveNotifications();
            return;
        }
        rebuildCoordinatorFromCurrentRecords();
    }

    synchronized Set<BoopNotificationChannelInfo> observedChannels() {
        return settingsStore.observedChannels();
    }

    synchronized void observeChannel(BoopNotificationChannelInfo channelInfo) {
        settingsStore.recordObservedChannel(channelInfo);
    }

    synchronized void attachListener(BoopNotificationListenerService listener) {
        attachedListener = listener;
    }

    synchronized void detachListener(BoopNotificationListenerService listener) {
        if (attachedListener == listener) {
            attachedListener = null;
        }
    }

    synchronized BoopNotificationCoordinator.Decision post(RuntimeRecord record, long nowMs) {
        if (record == null) {
            return BoopNotificationCoordinator.Decision.ignore();
        }
        BoopNotificationEnvelope envelope = record.envelope();
        if (!BoopNotificationPolicy.allows(settings, envelope.packageName(), envelope.channelId())) {
            return BoopNotificationCoordinator.Decision.ignore();
        }
        records.put(envelope.key(), record);
        return coordinator.onPosted(envelope, nowMs, settings);
    }

    synchronized void rebuild(Collection<RuntimeRecord> activeRecords) {
        records.clear();
        List<BoopNotificationEnvelope> envelopes = new ArrayList<>();
        if (activeRecords != null) {
            for (RuntimeRecord record : activeRecords) {
                if (record == null) continue;
                BoopNotificationEnvelope envelope = record.envelope();
                if (!BoopNotificationPolicy.allows(
                        settings, envelope.packageName(), envelope.channelId())) {
                    continue;
                }
                records.put(envelope.key(), record);
                envelopes.add(envelope);
            }
        }
        coordinator.rebuild(envelopes, settings);
    }

    synchronized void remove(String key) {
        if (key == null) return;
        records.remove(key);
        coordinator.onRemoved(key);
    }

    synchronized void onPresentationDismissed() {
        coordinator.onPresentationDismissed();
    }

    synchronized RuntimeRecord record(String key) {
        return key == null ? null : records.get(key);
    }

    synchronized List<BoopNotificationEnvelope> activeNotifications() {
        return coordinator.activeNotifications();
    }

    synchronized List<BoopNotificationEnvelope> visibleBundle() {
        return coordinator.visibleBundle();
    }

    synchronized void cancelAfterSuccessfulAutoCancelTap(String key) {
        if (key == null || key.isEmpty() || attachedListener == null) {
            return;
        }
        try {
            attachedListener.cancelNotification(key);
        } catch (SecurityException ignored) {
            // Source notification remains authoritative if Android rejects cancellation.
        }
    }

    private void rebuildCoordinatorFromCurrentRecords() {
        List<RuntimeRecord> snapshot = new ArrayList<>(records.values());
        records.clear();
        List<BoopNotificationEnvelope> envelopes = new ArrayList<>();
        for (RuntimeRecord record : snapshot) {
            BoopNotificationEnvelope envelope = record.envelope();
            if (!BoopNotificationPolicy.allows(settings, envelope.packageName(), envelope.channelId())) {
                continue;
            }
            records.put(envelope.key(), record);
            envelopes.add(envelope);
        }
        coordinator.rebuild(envelopes, settings);
    }
}

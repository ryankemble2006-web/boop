package com.boop.alpha1;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final BoopNotificationOverlayController overlayController;
    private final BoopNotificationCue cue;

    private BoopNotificationSettingsState settings;
    private BoopNotificationListenerService attachedListener;
    private BoopNotificationHost wallHost;
    private BoopNotificationSurface activeSurface;

    private BoopNotificationRuntime(Application application) {
        this.application = application;
        this.settingsStore = new BoopNotificationSettingsStore(application);
        this.settings = settingsStore.load();
        this.coordinator = new BoopNotificationCoordinator(BURST_WINDOW_MS);
        this.overlayController = new BoopNotificationOverlayController(application, this);
        this.cue = new BoopNotificationCue(application);
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
        scheduleHideAllSurfaces();
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
        BoopNotificationCoordinator.Decision decision = coordinator.onPosted(envelope, nowMs, settings);
        if (decision.kind() != BoopNotificationCoordinator.Kind.IGNORE) {
            schedulePresentation(decision.kind());
            if (BoopNotificationCuePolicy.shouldPlay(decision.playCue(), record.channelInfo())) {
                scheduleCue();
            }
        }
        return decision;
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
        scheduleHideAllSurfaces();
    }

    synchronized void remove(String key) {
        if (key == null) return;
        records.remove(key);
        coordinator.onRemoved(key);
        if (coordinator.visibleBundle().isEmpty()) {
            scheduleHideAllSurfaces();
        } else {
            schedulePresentation(BoopNotificationCoordinator.Kind.UPDATE);
        }
    }

    synchronized void onPresentationDismissed() {
        coordinator.onPresentationDismissed();
        scheduleHideAllSurfaces();
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

    synchronized void registerWallHost(BoopNotificationHost host) {
        if (host == null) return;
        wallHost = host;
        if (!coordinator.visibleBundle().isEmpty()) {
            schedulePresentation(BoopNotificationCoordinator.Kind.UPDATE);
        }
    }

    synchronized void unregisterWallHost(BoopNotificationHost host) {
        if (wallHost != host) return;
        wallHost = null;
        if (activeSurface == BoopNotificationSurface.IN_PLACE) {
            host.hide();
            activeSurface = null;
            if (!coordinator.visibleBundle().isEmpty()) {
                schedulePresentation(BoopNotificationCoordinator.Kind.PRESENT);
            }
        }
    }

    void transitionAfterUnlock() {
        schedulePresentation(BoopNotificationCoordinator.Kind.UPDATE);
    }

    synchronized void onPresentationFailed(BoopNotificationSurface surface) {
        if (surface == activeSurface) {
            activeSurface = null;
        }
        coordinator.onPresentationDismissed();
        scheduleHideAllSurfaces();
    }

    BoopNotificationTapLauncher.Result openNotification(Context context, String key) {
        final RuntimeRecord current;
        synchronized (this) {
            current = key == null ? null : records.get(key);
        }
        if (current == null) {
            return BoopNotificationTapLauncher.Result.CANCELLED;
        }

        BoopNotificationTapLauncher.Result result = BoopNotificationTapLauncher.send(
                context,
                current.contentIntent(),
                Build.VERSION.SDK_INT);
        if (result != BoopNotificationTapLauncher.Result.OPENED) {
            return result;
        }

        if (BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(
                current.envelope().autoCancel(), true)) {
            cancelAfterSuccessfulAutoCancelTap(current.envelope().key());
        }
        onPresentationDismissed();
        return result;
    }

    boolean openInbox(Context context) {
        if (context == null) return false;
        Intent intent = new Intent(context, BoopNotificationInboxActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            context.startActivity(intent);
            onPresentationDismissed();
            return true;
        } catch (RuntimeException unavailable) {
            return false;
        }
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

    private void schedulePresentation(BoopNotificationCoordinator.Kind kind) {
        mainHandler.post(() -> presentVisibleBundle(kind));
    }

    private void scheduleCue() {
        mainHandler.post(cue::play);
    }

    private void scheduleHideAllSurfaces() {
        mainHandler.post(this::hideAllSurfaces);
    }

    private void presentVisibleBundle(BoopNotificationCoordinator.Kind kind) {
        final List<BoopNotificationEnvelope> bundle;
        final long timeoutMs;
        final BoopNotificationHost currentWallHost;
        final BoopNotificationSurface previousSurface;
        synchronized (this) {
            bundle = coordinator.visibleBundle();
            timeoutMs = settings.timeoutMs();
            currentWallHost = wallHost;
            previousSurface = activeSurface;
        }
        if (bundle.isEmpty()) {
            hideAllSurfaces();
            return;
        }

        PowerManager power = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard =
                (KeyguardManager) application.getSystemService(Context.KEYGUARD_SERVICE);
        boolean interactive = power != null && power.isInteractive();
        boolean keyguardLocked = keyguard != null && keyguard.isKeyguardLocked();
        BoopNotificationSurface surface = BoopNotificationSurfaceSelector.choose(
                interactive,
                keyguardLocked,
                currentWallHost != null);
        BoopNotificationPresentation presentation = BoopNotificationPresentation.from(
                bundle,
                surface,
                surface == BoopNotificationSurface.LOCKED);

        if (surface == BoopNotificationSurface.LOCKED) {
            if (currentWallHost != null) currentWallHost.hide();
            overlayController.hide();
            synchronized (this) {
                activeSurface = BoopNotificationSurface.LOCKED;
            }
            Intent intent = new Intent(application, BoopNotificationLockActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {
                application.startActivity(intent);
            } catch (RuntimeException unavailable) {
                onPresentationFailed(BoopNotificationSurface.LOCKED);
            }
            return;
        }

        if (surface == BoopNotificationSurface.IN_PLACE) {
            overlayController.hide();
            if (currentWallHost == null) {
                onPresentationFailed(BoopNotificationSurface.IN_PLACE);
                return;
            }
            synchronized (this) {
                activeSurface = BoopNotificationSurface.IN_PLACE;
            }
            if (previousSurface == BoopNotificationSurface.IN_PLACE
                    && kind == BoopNotificationCoordinator.Kind.UPDATE) {
                currentWallHost.update(presentation, timeoutMs);
            } else {
                currentWallHost.show(presentation, timeoutMs);
            }
            return;
        }

        if (currentWallHost != null) currentWallHost.hide();
        synchronized (this) {
            activeSurface = BoopNotificationSurface.OVERLAY;
        }
        if (previousSurface == BoopNotificationSurface.OVERLAY
                && kind == BoopNotificationCoordinator.Kind.UPDATE) {
            overlayController.update(presentation, timeoutMs);
        } else {
            overlayController.show(presentation, timeoutMs);
        }
    }

    private void hideAllSurfaces() {
        BoopNotificationHost currentWallHost;
        synchronized (this) {
            currentWallHost = wallHost;
            activeSurface = null;
        }
        if (currentWallHost != null) currentWallHost.hide();
        overlayController.hide();
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

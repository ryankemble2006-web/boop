package com.boop.shieldhome;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.view.accessibility.AccessibilityManager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Android media-session boundary for the standalone Shield launcher.
 *
 * Primary discovery on Shield comes from the already-enabled BOOP Accessibility service, which
 * passes only MediaSession.Token objects from media-style notification events. A previously
 * granted Notification Listener remains a compatible fallback for enumerating active sessions.
 */
public final class ShieldNowPlayingManager {
    private static final String ENABLED_LISTENERS = "enabled_notification_listeners";
    private static volatile ShieldNowPlayingManager instance;

    private final Context applicationContext;
    private final NotificationManager notificationManager;
    private final MediaSessionManager mediaSessionManager;
    private final ComponentName listenerComponent;
    private final Handler mainHandler;
    private final ShieldHomeStore store;
    private final NowPlayingState state = new NowPlayingState();
    private final LinkedHashMap<MediaSession.Token, Binding> bindings = new LinkedHashMap<>();

    private final MediaSessionManager.OnActiveSessionsChangedListener activeSessionsChanged =
            this::reconcileControllers;

    private boolean listenerConnected;
    private boolean activeSessionsListenerRegistered;
    private boolean reconciling;
    private boolean reconcilePending;
    private long nextSessionId = 1L;
    private long selectedId;
    private MediaController selectedController;
    private String preferredPackage;

    public static ShieldNowPlayingManager get(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        ShieldNowPlayingManager local = instance;
        if (local == null) {
            synchronized (ShieldNowPlayingManager.class) {
                local = instance;
                if (local == null) {
                    local = new ShieldNowPlayingManager(context.getApplicationContext());
                    instance = local;
                }
            }
        }
        return local;
    }

    private ShieldNowPlayingManager(Context applicationContext) {
        this.applicationContext = applicationContext;
        notificationManager = applicationContext.getSystemService(NotificationManager.class);
        mediaSessionManager = applicationContext.getSystemService(MediaSessionManager.class);
        listenerComponent = new ComponentName(
                applicationContext, ShieldNowPlayingListenerService.class);
        mainHandler = new Handler(Looper.getMainLooper());
        store = new ShieldHomeStore(applicationContext);
        preferredPackage = store.nowPlayingPlayerPackage();
    }

    public NowPlayingState state() {
        return state;
    }

    /** Refreshes the optional Notification Listener fallback without disturbing Accessibility media. */
    public void refreshAccess() {
        runOnMain(this::refreshListenerAccessOnMain);
    }

    /** Media access now follows the already-approved BOOP Home Override Accessibility service. */
    public boolean hasAccess() {
        return isAccessibilityBridgeEnabled();
    }

    /** Opens the same Shield Accessibility setup used by BOOP Home Override. */
    public boolean openAccessSettings(Activity activity) {
        if (activity == null) {
            return false;
        }
        try {
            activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return true;
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            return false;
        }
    }

    public void setPreferredPackage(String packageName) {
        String cleaned = packageName == null ? "" : packageName.trim();
        store.setNowPlayingPlayerPackage(cleaned);
        runOnMain(() -> {
            preferredPackage = cleaned;
            publishSelection();
        });
    }

    public void previous() {
        runTransport(NowPlayingSnapshot::canPrevious, controls -> controls.skipToPrevious());
    }

    public void rewind() {
        runTransport(NowPlayingSnapshot::canRewind, controls -> controls.rewind());
    }

    public void togglePlayPause() {
        runOnMain(() -> {
            NowPlayingSnapshot snapshot = state.current();
            MediaController controller = selectedController;
            if (snapshot == null || controller == null || !snapshot.canPlayPause()) {
                return;
            }
            MediaController.TransportControls controls = controller.getTransportControls();
            if (snapshot.isPlaying()) {
                controls.pause();
            } else {
                controls.play();
            }
        });
    }

    public void fastForward() {
        runTransport(NowPlayingSnapshot::canFastForward, controls -> controls.fastForward());
    }

    public void next() {
        runTransport(NowPlayingSnapshot::canNext, controls -> controls.skipToNext());
    }

    /** Reopens the package that owns the currently selected media session, when launchable. */
    public boolean openSource(Activity activity) {
        if (activity == null) {
            return false;
        }
        NowPlayingSnapshot snapshot = state.current();
        if (snapshot == null || snapshot.packageName().isEmpty()) {
            return false;
        }
        Intent launch = activity.getPackageManager().getLaunchIntentForPackage(snapshot.packageName());
        if (launch == null) {
            return false;
        }
        try {
            activity.startActivity(launch);
            return true;
        } catch (ActivityNotFoundException | SecurityException unavailable) {
            return false;
        }
    }

    /**
     * Registers a media session discovered by the BOOP Accessibility service.
     * No notification text, actions or other notification payload is passed into this manager.
     */
    void onAccessibilityMediaSession(MediaSession.Token token) {
        if (token == null) {
            return;
        }
        runOnMain(() -> {
            Binding binding = bindings.get(token);
            if (binding == null) {
                MediaController controller;
                try {
                    controller = new MediaController(applicationContext, token);
                } catch (RuntimeException invalidToken) {
                    return;
                }
                binding = new Binding(controller, allocateSessionId());
                try {
                    binding.register();
                } catch (RuntimeException unavailable) {
                    return;
                }
                bindings.put(token, binding);
            }
            binding.accessibilityBacked = true;
            publishSelection();
        });
    }

    void onListenerConnected() {
        runOnMain(() -> {
            listenerConnected = true;
            refreshListenerAccessOnMain();
        });
    }

    void onListenerDisconnected() {
        runOnMain(() -> {
            listenerConnected = false;
            stopListenerObservation();
        });
    }

    private void refreshListenerAccessOnMain() {
        boolean granted = isNotificationListenerAccessGranted();
        if (!granted) {
            stopListenerObservation();
            return;
        }
        if (!listenerConnected) {
            try {
                NotificationListenerService.requestRebind(listenerComponent);
            } catch (SecurityException ignored) {
                // Accessibility remains the primary path. This fallback can fail closed.
            }
            stopListenerObservation();
            return;
        }
        startListenerObservation();
    }

    private boolean isNotificationListenerAccessGranted() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                return notificationManager != null
                        && notificationManager.isNotificationListenerAccessGranted(listenerComponent);
            }
            String enabled = Settings.Secure.getString(
                    applicationContext.getContentResolver(), ENABLED_LISTENERS);
            if (enabled == null || enabled.isEmpty()) {
                return false;
            }
            for (String flattened : enabled.split(":")) {
                ComponentName component = ComponentName.unflattenFromString(flattened);
                if (listenerComponent.equals(component)) {
                    return true;
                }
            }
        } catch (SecurityException denied) {
            return false;
        }
        return false;
    }

    private boolean isAccessibilityBridgeEnabled() {
        AccessibilityManager manager =
                (AccessibilityManager) applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) {
            return false;
        }
        List<AccessibilityServiceInfo> enabled;
        try {
            enabled = manager.getEnabledAccessibilityServiceList(
                    AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        } catch (RuntimeException unavailable) {
            return false;
        }
        if (enabled == null) {
            return false;
        }
        String serviceName = ShieldHomeOverrideService.class.getName();
        for (AccessibilityServiceInfo info : enabled) {
            ResolveInfo resolveInfo = info == null ? null : info.getResolveInfo();
            if (resolveInfo == null || resolveInfo.serviceInfo == null) {
                continue;
            }
            if (applicationContext.getPackageName().equals(resolveInfo.serviceInfo.packageName)
                    && serviceName.equals(resolveInfo.serviceInfo.name)) {
                return true;
            }
        }
        return false;
    }

    private void startListenerObservation() {
        if (mediaSessionManager == null) {
            stopListenerObservation();
            return;
        }
        try {
            if (!activeSessionsListenerRegistered) {
                mediaSessionManager.addOnActiveSessionsChangedListener(
                        activeSessionsChanged, listenerComponent, mainHandler);
                activeSessionsListenerRegistered = true;
            }
            reconcileControllers(mediaSessionManager.getActiveSessions(listenerComponent));
        } catch (SecurityException denied) {
            stopListenerObservation();
        }
    }

    /** Stops only Notification Listener-owned discovery; Accessibility-backed sessions survive. */
    private void stopListenerObservation() {
        if (activeSessionsListenerRegistered && mediaSessionManager != null) {
            try {
                mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsChanged);
            } catch (SecurityException | IllegalStateException ignored) {
                // Access revocation can race normal listener cleanup.
            }
        }
        activeSessionsListenerRegistered = false;

        LinkedHashMap<MediaSession.Token, Binding> retained = new LinkedHashMap<>();
        for (Map.Entry<MediaSession.Token, Binding> entry
                : new ArrayList<>(bindings.entrySet())) {
            Binding binding = entry.getValue();
            binding.listenerBacked = false;
            if (binding.accessibilityBacked) {
                retained.put(entry.getKey(), binding);
            } else {
                binding.unregister();
            }
        }
        bindings.clear();
        bindings.putAll(retained);
        publishSelection();
    }

    private void reconcileControllers(List<MediaController> controllers) {
        if (!listenerConnected) {
            return;
        }
        if (reconciling) {
            reconcilePending = true;
            return;
        }
        reconciling = true;
        try {
            List<MediaController> pendingControllers = controllers;
            do {
                reconcilePending = false;
                reconcileOnce(pendingControllers);
                pendingControllers = null;
                if (reconcilePending && mediaSessionManager != null) {
                    try {
                        pendingControllers = mediaSessionManager.getActiveSessions(listenerComponent);
                    } catch (SecurityException denied) {
                        stopListenerObservation();
                        return;
                    }
                }
            } while (reconcilePending);
        } finally {
            reconciling = false;
        }
        publishSelection();
    }

    private void reconcileOnce(List<MediaController> controllers) {
        List<MediaController> safeControllers =
                controllers == null ? Collections.emptyList() : controllers;
        LinkedHashMap<MediaSession.Token, Binding> ordered = new LinkedHashMap<>();

        for (MediaController controller : safeControllers) {
            if (controller == null || controller.getSessionToken() == null) {
                continue;
            }
            MediaSession.Token token = controller.getSessionToken();
            if (ordered.containsKey(token)) {
                continue;
            }
            Binding binding = bindings.get(token);
            if (binding == null) {
                binding = new Binding(controller, allocateSessionId());
                binding.register();
            }
            binding.listenerBacked = true;
            ordered.put(token, binding);
        }

        for (Map.Entry<MediaSession.Token, Binding> existing
                : new ArrayList<>(bindings.entrySet())) {
            if (ordered.containsKey(existing.getKey())) {
                continue;
            }
            Binding binding = existing.getValue();
            binding.listenerBacked = false;
            if (binding.accessibilityBacked) {
                ordered.put(existing.getKey(), binding);
            } else {
                binding.unregister();
            }
        }
        bindings.clear();
        bindings.putAll(ordered);
    }

    private void removeBinding(MediaSession.Token token) {
        if (token == null) {
            return;
        }
        Binding removed = bindings.remove(token);
        if (removed == null) {
            return;
        }
        removed.unregister();
        publishSelection();
    }

    private long allocateSessionId() {
        long id = nextSessionId++;
        if (id == 0L) {
            id = nextSessionId++;
        }
        return id;
    }

    private void publishSelection() {
        if (reconciling) {
            reconcilePending = true;
            return;
        }
        List<NowPlayingSelectionPolicy.Candidate> candidates = new ArrayList<>();
        for (Binding binding : bindings.values()) {
            PlaybackState playback = binding.controller.getPlaybackState();
            int playbackState = playback == null ? PlaybackState.STATE_NONE : playback.getState();
            candidates.add(new NowPlayingSelectionPolicy.Candidate(
                    binding.id,
                    binding.controller.getPackageName(),
                    playbackState));
        }

        long chosen = NowPlayingSelectionPolicy.select(candidates, selectedId, preferredPackage);
        selectedId = chosen;
        selectedController = null;
        if (chosen == 0L) {
            state.update(null);
            return;
        }

        for (Binding binding : bindings.values()) {
            if (binding.id != chosen) {
                continue;
            }
            selectedController = binding.controller;
            state.update(snapshot(binding));
            return;
        }

        selectedId = 0L;
        state.update(null);
    }

    private NowPlayingSnapshot snapshot(Binding binding) {
        MediaController controller = binding.controller;
        PlaybackState playback = controller.getPlaybackState();
        MediaMetadata metadata = controller.getMetadata();

        int playbackState = playback == null ? PlaybackState.STATE_NONE : playback.getState();
        long actions = playback == null ? 0L : playback.getActions();
        long position = playback == null ? 0L : playback.getPosition();
        float speed = playback == null ? 0f : playback.getPlaybackSpeed();
        long updateTime = playback == null ? 0L : playback.getLastPositionUpdateTime();

        String title = metadataText(metadata, MediaMetadata.METADATA_KEY_TITLE);
        if (title.isEmpty()) {
            title = metadataText(metadata, MediaMetadata.METADATA_KEY_DISPLAY_TITLE);
        }
        String subtitle = metadataText(metadata, MediaMetadata.METADATA_KEY_ARTIST);
        if (subtitle.isEmpty()) {
            subtitle = metadataText(metadata, MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        }
        if (subtitle.isEmpty()) {
            subtitle = metadataText(metadata, MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        }
        long duration = metadata == null ? 0L : metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);

        return new NowPlayingSnapshot(
                binding.id,
                controller.getPackageName(),
                title,
                subtitle,
                playbackState,
                actions,
                position,
                duration,
                speed,
                updateTime,
                artwork(metadata));
    }

    private static String metadataText(MediaMetadata metadata, String key) {
        if (metadata == null) {
            return "";
        }
        CharSequence value = metadata.getText(key);
        return value == null ? "" : value.toString().trim();
    }

    private Bitmap artwork(MediaMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        for (String key : new String[] {
                MediaMetadata.METADATA_KEY_ART,
                MediaMetadata.METADATA_KEY_ALBUM_ART,
                MediaMetadata.METADATA_KEY_DISPLAY_ICON
        }) {
            Bitmap bitmap = metadata.getBitmap(key);
            if (bitmap != null) {
                return bitmap;
            }
        }
        for (String key : new String[] {
                MediaMetadata.METADATA_KEY_ART_URI,
                MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI
        }) {
            Bitmap bitmap = decodeLocalArtwork(metadata.getString(key));
            if (bitmap != null) {
                return bitmap;
            }
        }
        return null;
    }

    private Bitmap decodeLocalArtwork(String rawUri) {
        if (rawUri == null || rawUri.trim().isEmpty()) {
            return null;
        }
        Uri uri;
        try {
            uri = Uri.parse(rawUri.trim());
        } catch (RuntimeException malformed) {
            return null;
        }
        String scheme = uri.getScheme();
        if (!"content".equalsIgnoreCase(scheme)
                && !"file".equalsIgnoreCase(scheme)
                && !"android.resource".equalsIgnoreCase(scheme)) {
            return null;
        }
        try (InputStream stream = applicationContext.getContentResolver().openInputStream(uri)) {
            return stream == null ? null : BitmapFactory.decodeStream(stream);
        } catch (Exception unavailable) {
            return null;
        }
    }

    private void runTransport(SnapshotCapability capability, TransportAction action) {
        runOnMain(() -> {
            NowPlayingSnapshot snapshot = state.current();
            MediaController controller = selectedController;
            if (snapshot == null || controller == null || !capability.allowed(snapshot)) {
                return;
            }
            action.run(controller.getTransportControls());
        });
    }

    private void runOnMain(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            mainHandler.post(action);
        }
    }

    private interface SnapshotCapability {
        boolean allowed(NowPlayingSnapshot snapshot);
    }

    private interface TransportAction {
        void run(MediaController.TransportControls controls);
    }

    private final class Binding {
        private final MediaController controller;
        private final long id;
        private boolean registered;
        private boolean accessibilityBacked;
        private boolean listenerBacked;
        private final MediaController.Callback callback = new MediaController.Callback() {
            @Override public void onMetadataChanged(MediaMetadata metadata) {
                publishSelection();
            }

            @Override public void onPlaybackStateChanged(PlaybackState playbackState) {
                publishSelection();
            }

            @Override public void onSessionDestroyed() {
                MediaSession.Token token = controller.getSessionToken();
                mainHandler.post(() -> removeBinding(token));
            }
        };

        private Binding(MediaController controller, long id) {
            this.controller = controller;
            this.id = id;
        }

        private void register() {
            if (registered) {
                return;
            }
            registered = true;
            controller.registerCallback(callback, mainHandler);
        }

        private void unregister() {
            if (!registered) {
                return;
            }
            registered = false;
            try {
                controller.unregisterCallback(callback);
            } catch (IllegalStateException ignored) {
                // Session destruction can race normal cleanup.
            }
        }
    }
}

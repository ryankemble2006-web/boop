package com.boop.shieldoverlay;

import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DeezerMediaListenerService extends NotificationListenerService {
    private DeezerPuppetAccess access;
    private Runnable unsubscribeAccess;
    private DeezerSessionObserver observer;
    private boolean listenerConnected;

    @Override
    public void onCreate() {
        super.onCreate();
        access = DeezerPuppetAccess.get(this);
        unsubscribeAccess = access.state().subscribe(this::accessChanged);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        listenerConnected = true;
        access.refresh();
        access.setListenerConnected(true);
    }

    @Override
    public void onListenerDisconnected() {
        listenerConnected = false;
        stopObservation();
        access.setListenerConnected(false);
        access.refresh();
        super.onListenerDisconnected();
    }

    @Override
    public void onDestroy() {
        listenerConnected = false;
        stopObservation();
        if (unsubscribeAccess != null) {
            unsubscribeAccess.run();
            unsubscribeAccess = null;
        }
        if (access != null) {
            access.setListenerConnected(false);
        }
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        // Intentionally ignored: this service observes only Deezer media-session state.
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification notification) {
        // Intentionally ignored: no notification payload is read, stored, or cancelled.
    }

    private void accessChanged(MediaPuppetState.Snapshot snapshot) {
        if (listenerConnected
                && snapshot.enabled
                && snapshot.granted
                && snapshot.connected) {
            startObservation();
        } else {
            stopObservation();
        }
    }

    private void startObservation() {
        if (observer == null) {
            MediaSessionManager manager = getSystemService(MediaSessionManager.class);
            if (manager == null) {
                access.state().updateSession(0L, null);
                return;
            }
            observer = new DeezerSessionObserver(
                    new AndroidPlatform(
                            manager,
                            new ComponentName(this, DeezerMediaListenerService.class),
                            new Handler(Looper.getMainLooper())),
                    access.state());
        }
        observer.connect();
    }

    private void stopObservation() {
        if (observer != null) {
            observer.disconnect();
        }
    }

    private static final class AndroidPlatform implements DeezerSessionObserver.Platform {
        private final MediaSessionManager manager;
        private final ComponentName listenerComponent;
        private final Handler mainHandler;
        private Runnable registeredRunnable;
        private MediaSessionManager.OnActiveSessionsChangedListener registeredListener;

        private AndroidPlatform(
                MediaSessionManager manager,
                ComponentName listenerComponent,
                Handler mainHandler) {
            this.manager = manager;
            this.listenerComponent = listenerComponent;
            this.mainHandler = mainHandler;
        }

        @Override
        public void register(Runnable sessionsChanged) {
            registeredRunnable = sessionsChanged;
            registeredListener = controllers -> sessionsChanged.run();
            manager.addOnActiveSessionsChangedListener(
                    registeredListener, listenerComponent, mainHandler);
        }

        @Override
        public void unregister(Runnable sessionsChanged) {
            if (registeredRunnable != sessionsChanged || registeredListener == null) {
                return;
            }
            MediaSessionManager.OnActiveSessionsChangedListener listener = registeredListener;
            registeredRunnable = null;
            registeredListener = null;
            manager.removeOnActiveSessionsChangedListener(listener);
        }

        @Override
        public List<DeezerSessionObserver.SessionPort> sessions() {
            List<MediaController> controllers = manager.getActiveSessions(listenerComponent);
            if (controllers == null || controllers.isEmpty()) {
                return Collections.emptyList();
            }
            List<DeezerSessionObserver.SessionPort> ports = new ArrayList<>(controllers.size());
            for (MediaController controller : controllers) {
                if (controller != null) {
                    ports.add(new AndroidSession(controller, mainHandler));
                }
            }
            return ports;
        }
    }

    private static final class AndroidSession implements DeezerSessionObserver.SessionPort {
        private final MediaController controller;
        private final Handler mainHandler;
        private final Map<DeezerSessionObserver.Callback, MediaController.Callback> callbacks =
                new IdentityHashMap<>();

        private AndroidSession(MediaController controller, Handler mainHandler) {
            this.controller = controller;
            this.mainHandler = mainHandler;
        }

        @Override
        public Object token() {
            return controller.getSessionToken();
        }

        @Override
        public String packageName() {
            return controller.getPackageName();
        }

        @Override
        public Integer playbackState() {
            PlaybackState playbackState = controller.getPlaybackState();
            return playbackState == null ? null : playbackState.getState();
        }

        @Override
        public void register(DeezerSessionObserver.Callback callback) {
            MediaController.Callback androidCallback = new MediaController.Callback() {
                @Override
                public void onPlaybackStateChanged(PlaybackState state) {
                    callback.stateChanged(state == null ? null : state.getState());
                }

                @Override
                public void onSessionDestroyed() {
                    callback.destroyed();
                }
            };
            callbacks.put(callback, androidCallback);
            controller.registerCallback(androidCallback, mainHandler);
        }

        @Override
        public void unregister(DeezerSessionObserver.Callback callback) {
            MediaController.Callback androidCallback = callbacks.remove(callback);
            if (androidCallback != null) {
                controller.unregisterCallback(androidCallback);
            }
        }
    }
}

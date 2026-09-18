package com.boop.shieldoverlay;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/** Android/HA adapter. Uses the existing credentials and canonical device-room preference. */
public final class RoomPanelSession implements AutoCloseable {
    private final Context context;
    private final BoopPreferences preferences;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService auth = Executors.newSingleThreadExecutor();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS).pingInterval(20, TimeUnit.SECONDS).build();
    private final RoomPanelController controller;
    private boolean closed;

    public RoomPanelSession(Context context, RoomPanelController.Listener listener) {
        this.context = context.getApplicationContext();
        preferences = new BoopPreferences(this.context);
        controller = new RoomPanelController(preferences::selectedRoom, (room, events) -> {
            Link link = new Link(room, events);
            link.authenticate();
            return link;
        }, (delay, task) -> {
            main.postDelayed(task, delay);
            return () -> main.removeCallbacks(task);
        }, listener);
    }

    public void start() { if (!closed) controller.start(); }
    public void stop() { if (!closed) controller.stop(); }
    public void toggle(long generation, String entityId) { if (!closed) controller.toggle(generation, entityId); }
    @Override public void close() {
        if (closed) return;
        controller.stop(); closed = true;
        auth.shutdownNow();
        main.removeCallbacksAndMessages(null);
        client.dispatcher().cancelAll();
        client.connectionPool().evictAll();
        client.dispatcher().executorService().shutdown();
    }

    private final class Link implements RoomPanelController.Connection {
        private final AreaInfo room;
        private final RoomPanelController.Events events;
        private volatile boolean stopped;
        private HomeAssistantWebSocket socket;
        private HomeAssistantRepository repository;
        private HomeAssistantWebSocket.Subscription subscription;

        Link(AreaInfo room, RoomPanelController.Events events) { this.room = room; this.events = events; }
        void authenticate() {
            auth.execute(() -> {
                if (stopped) return;
                try {
                    HomeAssistantSession session = new HomeAssistantSession(
                            new SecureCredentialStore(context), new HomeAssistantAuthClient(client));
                    HomeAssistantSession.Access access = session.ensureAccessToken();
                    dispatch(() -> connect(access));
                } catch (Exception unavailable) { dispatch(() -> events.onOffline(false)); }
            });
        }
        void dispatch(Runnable task) { main.post(() -> { if (!stopped && !closed) task.run(); }); }
        boolean allowed() {
            AreaInfo selected = preferences.selectedRoom();
            return !stopped && selected != null && room.id().equals(selected.id());
        }
        void connect(HomeAssistantSession.Access access) {
            socket = new HomeAssistantWebSocket(client);
            try { socket.connect(access.baseUrl(), access.accessToken(), new HomeAssistantWebSocket.Listener() {
                public void onReady() { dispatch(Link.this::ready); }
                public void onOffline(String ignored) { dispatch(() -> events.onOffline(false)); }
                public void onReauthRequired(String ignored) { dispatch(() -> events.onOffline(true)); }
            }); } catch (RuntimeException unavailable) { events.onOffline(false); }
        }
        void ready() {
            HomeAssistantRepository.StateChangePort changes = (listener, callback) -> {
                if (!allowed()) { callback.onResult(null, "Room controls are inactive."); return; }
                socket.subscribeStateChanges(listener::onStateChanged, (sub, error) ->
                        callback.onResult(sub == null ? null : sub::cancel, error));
            };
            repository = new HomeAssistantRepository((type, body, callback) -> {
                if (!allowed()) { callback.onResult(false, null, "Room controls are inactive."); return; }
                try { socket.send(type, body, callback); }
                catch (RuntimeException unavailable) { callback.onResult(false, null, "Home Assistant is offline."); }
            }, changes);
            try {
                socket.subscribeStateChanges((id, value) -> dispatch(() -> events.onState(id, value)),
                        (sub, error) -> main.post(() -> {
                            if (stopped || closed) { if (sub != null) sub.cancel(); return; }
                            if (sub == null || error != null) { events.onOffline(false); return; }
                            subscription = sub;
                            events.onReady();
                        }));
            } catch (RuntimeException unavailable) { events.onOffline(false); }
        }
        public void load(RoomPanelController.LoadCallback callback) {
            if (!allowed() || repository == null) { callback.onResult(null, "Room controls are inactive."); return; }
            repository.loadDashboard(room, (data, error) -> dispatch(() -> callback.onResult(data, error)));
        }
        public void toggle(EntityCard card, RoomPanelController.ActionCallback callback) {
            if (!allowed() || repository == null || !RoomScopedEntities.belongsTo(room, card)) {
                callback.onResult(false, null, "Room controls are inactive."); return;
            }
            repository.toggleBinary(room, card, new HomeAssistantRepository.BinaryActionCallback() {
                public void onObservedState(EntityCard actual) { dispatch(() -> callback.onObserved(actual)); }
                public void onResult(boolean success, EntityCard actual, String error) {
                    dispatch(() -> callback.onResult(success, actual, error));
                }
            });
        }
        public void close() {
            stopped = true;
            if (subscription != null) { subscription.cancel(); subscription = null; }
            if (socket != null) socket.close();
            repository = null;
        }
    }
}

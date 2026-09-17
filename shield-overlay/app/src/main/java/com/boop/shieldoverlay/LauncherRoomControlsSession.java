package com.boop.shieldoverlay;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

/**
 * Launcher-owned Home Assistant session for the room chosen by BOOP's existing
 * "Set this device room" flow. No second room preference is introduced here.
 */
public final class LauncherRoomControlsSession implements AutoCloseable {
    public interface Listener {
        void onRoomState(AreaInfo room, HomeDashboardController.ViewState state);
    }

    private final Context context;
    private final BoopPreferences preferences;
    private final Listener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "boop-launcher-room-controls");
        thread.setDaemon(true);
        return thread;
    });
    private final OkHttpClient httpClient = new OkHttpClient.Builder().build();

    private HomeAssistantWebSocket socket;
    private HomeDashboardController controller;
    private int generation;
    private boolean closed;

    public LauncherRoomControlsSession(Context context, Listener listener) {
        if (context == null) throw new IllegalArgumentException("context is required");
        if (listener == null) throw new IllegalArgumentException("listener is required");
        this.context = context.getApplicationContext();
        this.preferences = new BoopPreferences(this.context);
        this.listener = listener;
    }

    /** Re-read the canonical BOOP room and rebuild the live room dashboard. */
    public void refresh() {
        if (closed) return;
        final int request = ++generation;
        closeTransport();

        AreaInfo room = preferences.selectedRoom();
        if (room == null) {
            emit(request, null, null);
            return;
        }

        executor.execute(() -> {
            try {
                HomeAssistantSession session = new HomeAssistantSession(
                        new SecureCredentialStore(context),
                        new HomeAssistantAuthClient(httpClient));
                HomeAssistantSession.Access access = session.ensureAccessToken();
                mainHandler.post(() -> connect(request, room, access));
            } catch (Exception unavailable) {
                mainHandler.post(() -> startOfflineController(
                        request, room, "Home Assistant is offline."));
            }
        });
    }

    /** Stop network work while the launcher is not visible; refresh() can restart it. */
    public void stop() {
        if (closed) return;
        ++generation;
        closeTransport();
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        ++generation;
        closeTransport();
        executor.shutdownNow();
    }

    private void connect(int request, AreaInfo room, HomeAssistantSession.Access access) {
        if (!current(request) || access == null) return;
        closeTransport();

        HomeAssistantWebSocket opened = new HomeAssistantWebSocket(httpClient);
        socket = opened;
        opened.connect(access.baseUrl(), access.accessToken(), new HomeAssistantWebSocket.Listener() {
            @Override public void onReady() {
                mainHandler.post(() -> {
                    if (!current(request) || socket != opened) return;
                    startLiveController(request, room, opened);
                });
            }

            @Override public void onOffline(String message) {
                mainHandler.post(() -> markOffline(
                        request, room, clean(message, "Home Assistant is offline.")));
            }

            @Override public void onReauthRequired(String message) {
                mainHandler.post(() -> markOffline(
                        request, room, "Home Assistant needs BOOP to pair again."));
            }
        });
    }

    private void startLiveController(
            int request,
            AreaInfo room,
            HomeAssistantWebSocket opened) {
        if (!current(request) || socket != opened) return;

        HomeAssistantRepository.StateChangePort stateChangePort = (stateListener, callback) ->
                opened.subscribeStateChanges(
                        stateListener::onStateChanged,
                        (subscription, error) -> callback.onResult(
                                subscription == null ? null : subscription::cancel,
                                error));
        HomeAssistantRepository repository = new HomeAssistantRepository(opened::send, stateChangePort);
        HomeDashboardController.RepositoryPort repositoryPort = new HomeDashboardController.RepositoryPort() {
            @Override public void loadDashboard(
                    AreaInfo selectedRoom,
                    HomeAssistantRepository.DashboardCallback callback) {
                try {
                    repository.loadDashboard(selectedRoom, callback);
                } catch (RuntimeException unavailable) {
                    callback.onResult(null, "Home Assistant is offline.");
                }
            }

            @Override public void toggleBinary(
                    EntityCard card,
                    HomeAssistantRepository.BinaryActionCallback callback) {
                try {
                    repository.toggleBinary(card, callback);
                } catch (RuntimeException unavailable) {
                    callback.onResult(false, null, "Home Assistant is offline.");
                }
            }
        };
        startController(request, room, repositoryPort);
    }

    private void startOfflineController(int request, AreaInfo room, String message) {
        if (!current(request)) return;
        HomeDashboardController.RepositoryPort offline = new HomeDashboardController.RepositoryPort() {
            @Override public void loadDashboard(
                    AreaInfo selectedRoom,
                    HomeAssistantRepository.DashboardCallback callback) {
                callback.onResult(null, message);
            }

            @Override public void toggleBinary(
                    EntityCard card,
                    HomeAssistantRepository.BinaryActionCallback callback) {
                callback.onResult(false, null, message);
            }
        };
        startController(request, room, offline);
    }

    private void startController(
            int request,
            AreaInfo room,
            HomeDashboardController.RepositoryPort repositoryPort) {
        if (!current(request)) return;
        if (controller != null) controller.stop();
        controller = new HomeDashboardController(
                room,
                repositoryPort,
                new HomeDashboardController.CachePort() {
                    @Override public EntityCard load(AreaInfo ignored) { return null; }
                    @Override public void save(AreaInfo ignored, EntityCard card) { }
                    @Override public void clear(AreaInfo ignored) { }
                },
                state -> mainHandler.post(() -> emit(request, room, state)));
        controller.start();
    }

    private void markOffline(int request, AreaInfo room, String message) {
        if (!current(request)) return;
        if (controller != null) {
            controller.markOffline(message);
        } else {
            startOfflineController(request, room, message);
        }
    }

    private void emit(int request, AreaInfo room, HomeDashboardController.ViewState state) {
        if (!current(request)) return;
        listener.onRoomState(room, state);
    }

    private boolean current(int request) {
        return !closed && request == generation;
    }

    private void closeTransport() {
        HomeDashboardController oldController = controller;
        controller = null;
        if (oldController != null) oldController.stop();

        HomeAssistantWebSocket oldSocket = socket;
        socket = null;
        if (oldSocket != null) oldSocket.close();
    }

    private static String clean(String value, String fallback) {
        if (value == null) return fallback;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? fallback : trimmed;
    }
}

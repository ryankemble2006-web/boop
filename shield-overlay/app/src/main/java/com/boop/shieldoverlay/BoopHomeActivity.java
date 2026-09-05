package com.boop.shieldoverlay;

import android.app.Activity;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public final class BoopHomeActivity extends Activity {
    private static final long FOUND_IT_BEAT_MS = 700L;
    private static final long DASHBOARD_RETRY_MIN_MS = 3000L;
    private static final long DASHBOARD_RETRY_MAX_MS = 15000L;

    private final FirstRunCoordinator firstRun = new FirstRunCoordinator();

    private Handler mainHandler;
    private ExecutorService pairingExecutor;
    private ExecutorService homeExecutor;
    private PairingGateController pairingController;
    private HomeAssistantWebSocket homeSocket;
    private BoopPreferences preferences;
    private Runnable expiryRunnable;
    private Runnable successRunnable;
    private Runnable dashboardReconnectRunnable;
    private TvPairingView pairingView;
    private TvRoomPickerView roomPickerView;

    private TvNavigationModel navigationModel;
    private FrameLayout navigationContent;
    private FocusCardView homeRailCard;
    private FocusCardView routinesRailCard;
    private FocusCardView settingsRailCard;
    private View currentPageFirstFocusable;
    private boolean homeShellVisible;

    private HomeDashboardController dashboardController;
    private HomeDashboardController.ViewState dashboardState;
    private TvHomeView homeView;
    private RoutinesController routinesController;
    private RoutinesController.ViewState routinesState;
    private TvRoutinesView routinesView;
    private int dashboardReconnectAttempt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepAwakeAndHideSystemUi();

        preferences = new BoopPreferences(this);
        mainHandler = new Handler(Looper.getMainLooper());
        expiryRunnable = () -> {
            PairingGateController controller = pairingController;
            if (controller != null) {
                controller.expireIfNeeded();
            }
        };
        successRunnable = this::advanceAfterPairingSuccess;
        dashboardReconnectRunnable = () -> {
            if (homeShellVisible && !isFinishing() && !isDestroyed()) {
                startHomeDashboard();
            }
        };
        showPairingGate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BoopOverlayController.hide(this);
    }

    @Override
    protected void onStop() {
        BoopOverlayController.show(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (homeShellVisible && navigationModel != null) {
            if (navigationModel.onBack()) {
                finish();
            } else {
                renderNavigationPage(navigationModel.page(), true);
            }
            return;
        }
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            keepAwakeAndHideSystemUi();
        }
    }

    @Override
    protected void onDestroy() {
        if (mainHandler != null) {
            if (expiryRunnable != null) {
                mainHandler.removeCallbacks(expiryRunnable);
            }
            if (successRunnable != null) {
                mainHandler.removeCallbacks(successRunnable);
            }
            if (dashboardReconnectRunnable != null) {
                mainHandler.removeCallbacks(dashboardReconnectRunnable);
            }
        }
        if (pairingController != null) {
            pairingController.close();
            pairingController = null;
        }
        closeRoutinesController();
        closeHomeSocket();
        dashboardController = null;
        dashboardState = null;
        homeView = null;
        routinesState = null;
        routinesView = null;
        if (pairingExecutor != null) {
            pairingExecutor.shutdownNow();
            pairingExecutor = null;
        }
        if (homeExecutor != null) {
            homeExecutor.shutdownNow();
            homeExecutor = null;
        }
        super.onDestroy();
    }

    private void keepAwakeAndHideSystemUi() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getDecorView();
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void showPairingGate() {
        clearNavigationShellState();
        firstRun.start(false, false);
        pairingView = new TvPairingView(this, this::restartPairing);
        roomPickerView = null;
        pairingView.showSearching(getString(R.string.pairing_title_searching));
        setContentView(pairingView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        createPairingController();
        pairingController.start();
    }

    private void createPairingController() {
        HomeAssistantDiscovery discovery = new HomeAssistantDiscovery(this);
        TemporaryTlsPairingServer server = new TemporaryTlsPairingServer();
        HomeAssistantAuthClient authClient = new HomeAssistantAuthClient(
                new OkHttpClient.Builder().build());
        SecureCredentialStore credentialStore = new SecureCredentialStore(this);

        PairingGateController.DiscoveryPort discoveryPort =
                new PairingGateController.DiscoveryPort() {
                    @Override
                    public void start(Listener listener) {
                        discovery.start(new HomeAssistantDiscovery.Listener() {
                            @Override
                            public void onDiscovered(DiscoveredHomeAssistant homeAssistant) {
                                listener.onDiscovered(homeAssistant);
                            }

                            @Override
                            public void onError(String message) {
                                listener.onError(message);
                            }
                        });
                    }

                    @Override
                    public void stop() {
                        discovery.stop();
                    }
                };

        PairingGateController.PairingServerPort serverPort =
                new PairingGateController.PairingServerPort() {
                    @Override
                    public PairingGateController.ServerEndpoint start(
                            InetAddress bindAddress,
                            PairingSession session,
                            Listener listener) throws Exception {
                        TemporaryTlsPairingServer.Endpoint endpoint = server.start(
                                bindAddress,
                                session,
                                listener::onAccepted);
                        return new PairingGateController.ServerEndpoint(
                                endpoint.host(),
                                endpoint.port(),
                                endpoint.certificatePinSha256());
                    }

                    @Override
                    public void close() {
                        server.close();
                    }
                };

        PairingGateController.AuthPort authPort = new PairingGateController.AuthPort() {
            @Override
            public AuthTokenSet exchangeAuthorizationCode(
                    String baseUrl,
                    String authorizationCode,
                    String clientId) throws Exception {
                return authClient.exchangeAuthorizationCode(baseUrl, authorizationCode, clientId);
            }

            @Override
            public AuthTokenSet refresh(
                    String baseUrl,
                    String refreshToken,
                    String clientId) throws Exception {
                return authClient.refresh(baseUrl, refreshToken, clientId);
            }
        };

        PairingGateController.CredentialStorePort storePort =
                new PairingGateController.CredentialStorePort() {
                    @Override
                    public StoredHomeAssistantCredential load() throws Exception {
                        return credentialStore.load();
                    }

                    @Override
                    public void save(StoredHomeAssistantCredential credential) throws Exception {
                        credentialStore.save(credential);
                    }
                };

        pairingExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "boop-ha-auth");
            thread.setDaemon(true);
            return thread;
        });

        pairingController = new PairingGateController(
                discoveryPort,
                serverPort,
                authPort,
                storePort,
                this::activeLanAddress,
                pairingExecutor,
                System::currentTimeMillis,
                state -> runOnUiThread(() -> renderPairingState(state)));
    }

    private void renderPairingState(PairingGateController.ViewState state) {
        if (state == null || isFinishing() || isDestroyed() || pairingView == null) {
            return;
        }
        mainHandler.removeCallbacks(expiryRunnable);
        mainHandler.removeCallbacks(successRunnable);

        PairingGateController.State value = state.state();
        if (value == PairingGateController.State.SEARCHING) {
            pairingView.showSearching(state.message());
            return;
        }

        if (value == PairingGateController.State.QR_READY) {
            try {
                pairingView.showQr(state.qrPayload(), pairingQrSizePx());
                long delayMs = Math.max(1L, state.expiresAtMs() - System.currentTimeMillis());
                mainHandler.postDelayed(expiryRunnable, delayMs);
            } catch (IllegalArgumentException couldNotDrawQr) {
                pairingController.close();
                pairingView.showRetry(getString(R.string.pairing_retry));
            }
            return;
        }

        if (value == PairingGateController.State.AUTHORIZING) {
            pairingView.showAuthorizing();
            return;
        }

        if (value == PairingGateController.State.CONNECTED) {
            firstRun.onPairingConnected();
            pairingView.showConnected();
            mainHandler.postDelayed(successRunnable, FOUND_IT_BEAT_MS);
            return;
        }

        if (value == PairingGateController.State.STALE
                || value == PairingGateController.State.FAILED) {
            pairingView.showRetry(state.message());
        }
    }

    private void advanceAfterPairingSuccess() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        boolean hasRoom = preferences != null && preferences.hasSelectedRoom();
        FirstRunCoordinator.State next = firstRun.afterPairingSuccess(hasRoom);
        if (next == FirstRunCoordinator.State.ROOM_PICKER) {
            showRoomPicker();
        } else if (next == FirstRunCoordinator.State.HOME) {
            showHomeShell();
        }
    }

    private void showRoomPicker() {
        clearNavigationShellState();
        closeHomeSocket();
        pairingView = null;
        roomPickerView = new TvRoomPickerView(this, new TvRoomPickerView.Listener() {
            @Override
            public void onSelected(AreaInfo area) {
                rememberRoomAndContinue(area);
            }

            @Override
            public void onRetry() {
                loadRooms();
            }
        });
        setContentView(roomPickerView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        roomPickerView.showLoading();
        loadRooms();
    }

    private void loadRooms() {
        TvRoomPickerView view = roomPickerView;
        if (view == null || isFinishing() || isDestroyed()) {
            return;
        }
        view.showLoading();
        closeHomeSocket();
        ensureHomeExecutor().execute(() -> {
            try {
                HomeAssistantSession session = new HomeAssistantSession(
                        new SecureCredentialStore(this),
                        new HomeAssistantAuthClient(new OkHttpClient.Builder().build()));
                HomeAssistantSession.Access access = session.ensureAccessToken();
                runOnUiThread(() -> connectForRooms(access));
            } catch (Exception e) {
                runOnUiThread(() -> showRoomError("I couldn't reach the house right now."));
            }
        });
    }

    private void connectForRooms(HomeAssistantSession.Access access) {
        if (access == null || roomPickerView == null || isFinishing() || isDestroyed()) {
            return;
        }
        closeHomeSocket();
        HomeAssistantWebSocket socket = new HomeAssistantWebSocket(
                new OkHttpClient.Builder().build());
        homeSocket = socket;
        socket.connect(access.baseUrl(), access.accessToken(), new HomeAssistantWebSocket.Listener() {
            @Override
            public void onReady() {
                HomeAssistantRepository repository = new HomeAssistantRepository(socket::send);
                repository.loadAreas((areas, error) -> runOnUiThread(() -> {
                    if (socket != homeSocket || roomPickerView == null
                            || isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (error != null) {
                        showRoomError(error);
                    } else {
                        showRoomAreas(areas);
                    }
                }));
            }

            @Override
            public void onOffline(String message) {
                runOnUiThread(() -> {
                    if (socket == homeSocket) {
                        showRoomError(message);
                    }
                });
            }

            @Override
            public void onReauthRequired(String message) {
                runOnUiThread(() -> {
                    if (socket == homeSocket) {
                        showRoomError("Home Assistant needs BOOP to pair again.");
                    }
                });
            }
        });
    }

    private void showRoomAreas(List<AreaInfo> areas) {
        if (roomPickerView != null) {
            roomPickerView.showAreas(areas);
        }
    }

    private void showRoomError(String message) {
        if (roomPickerView != null) {
            roomPickerView.showError(message);
        }
    }

    private void rememberRoomAndContinue(AreaInfo area) {
        if (area == null || preferences == null) {
            return;
        }
        preferences.setSelectedRoom(area);
        closeHomeSocket();
        firstRun.afterPairingSuccess(true);
        showHomeShell();
    }

    private ExecutorService ensureHomeExecutor() {
        if (homeExecutor == null || homeExecutor.isShutdown()) {
            homeExecutor = Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "boop-ha-home");
                thread.setDaemon(true);
                return thread;
            });
        }
        return homeExecutor;
    }

    private void closeHomeSocket() {
        HomeAssistantWebSocket socket = homeSocket;
        homeSocket = null;
        if (socket != null) {
            socket.close();
        }
    }

    private void showHomeShell() {
        closeRoutinesController();
        closeHomeSocket();
        pairingView = null;
        roomPickerView = null;
        homeShellVisible = true;
        navigationModel = new TvNavigationModel();
        dashboardController = null;
        dashboardState = null;
        routinesState = RoutinesController.ViewState.loading();
        routinesView = null;
        dashboardReconnectAttempt = 0;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(28), dp(26), dp(28), dp(26));

        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setPadding(0, dp(24), dp(20), 0);

        homeRailCard = createRailCard("Home", TvNavigationModel.Page.HOME);
        routinesRailCard = createRailCard("Routines", TvNavigationModel.Page.ROUTINES);
        settingsRailCard = createRailCard("Settings", TvNavigationModel.Page.SETTINGS);
        rail.addView(homeRailCard, railCardParams());
        rail.addView(routinesRailCard, railCardParams());
        rail.addView(settingsRailCard, railCardParams());

        navigationContent = new FrameLayout(this);

        root.addView(rail, new LinearLayout.LayoutParams(dp(270),
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(navigationContent, new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f));

        setContentView(root);
        renderNavigationPage(TvNavigationModel.Page.HOME, true);
        startHomeDashboard();
    }

    private void startHomeDashboard() {
        if (!homeShellVisible || preferences == null || isFinishing() || isDestroyed()) {
            return;
        }
        AreaInfo room = preferences.selectedRoom();
        if (room == null) {
            showRoomPicker();
            return;
        }

        mainHandler.removeCallbacks(dashboardReconnectRunnable);
        closeRoutinesController();
        closeHomeSocket();
        ensureHomeExecutor().execute(() -> {
            try {
                HomeAssistantSession session = new HomeAssistantSession(
                        new SecureCredentialStore(this),
                        new HomeAssistantAuthClient(new OkHttpClient.Builder().build()));
                HomeAssistantSession.Access access = session.ensureAccessToken();
                runOnUiThread(() -> connectForHomeDashboard(room, access));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (sameSelectedRoom(room) && homeShellVisible) {
                        showOfflineDashboard(room, "Home Assistant is offline.");
                        markRoutinesOffline();
                        scheduleHomeDashboardReconnect();
                    }
                });
            }
        });
    }

    private void connectForHomeDashboard(AreaInfo room, HomeAssistantSession.Access access) {
        if (access == null || !homeShellVisible || !sameSelectedRoom(room)
                || isFinishing() || isDestroyed()) {
            return;
        }

        closeRoutinesController();
        closeHomeSocket();
        HomeAssistantWebSocket socket = new HomeAssistantWebSocket(
                new OkHttpClient.Builder().build());
        homeSocket = socket;
        socket.connect(access.baseUrl(), access.accessToken(), new HomeAssistantWebSocket.Listener() {
            @Override
            public void onReady() {
                runOnUiThread(() -> {
                    if (socket != homeSocket || !homeShellVisible || !sameSelectedRoom(room)
                            || isFinishing() || isDestroyed()) {
                        return;
                    }
                    dashboardReconnectAttempt = 0;
                    HomeAssistantRepository.StateChangePort stateChangePort =
                            new HomeAssistantRepository.StateChangePort() {
                                @Override
                                public void subscribe(
                                        HomeAssistantRepository.StateChangePort.Listener listener,
                                        HomeAssistantRepository.StateChangePort.Callback callback) {
                                    socket.subscribeStateChanges(
                                            listener::onStateChanged,
                                            (subscription, error) -> {
                                                HomeAssistantRepository.StateChangePort.Subscription mapped =
                                                        subscription == null
                                                                ? null
                                                                : subscription::cancel;
                                                callback.onResult(mapped, error);
                                            });
                                }
                            };
                    HomeAssistantRepository repository = new HomeAssistantRepository(socket::send, stateChangePort);
                    HomeDashboardController.RepositoryPort repositoryPort =
                            new HomeDashboardController.RepositoryPort() {
                                @Override
                                public void loadDashboard(
                                        AreaInfo selectedRoom,
                                        HomeAssistantRepository.DashboardCallback callback) {
                                    try {
                                        repository.loadDashboard(selectedRoom, callback);
                                    } catch (RuntimeException unavailable) {
                                        callback.onResult(null, "Home Assistant is offline.");
                                    }
                                }

                                @Override
                                public void toggleBinary(
                                        EntityCard card,
                                        HomeAssistantRepository.BinaryActionCallback callback) {
                                    try {
                                        repository.toggleBinary(card, callback);
                                    } catch (RuntimeException unavailable) {
                                        callback.onResult(false, null, "Home Assistant is offline.");
                                    }
                                }
                            };
                    dashboardController = createDashboardController(room, repositoryPort);
                    dashboardController.start();

                    RoutinesRepository.StateChangePort routinesStateChangePort =
                            new RoutinesRepository.StateChangePort() {
                                @Override
                                public void subscribe(
                                        RoutinesRepository.StateChangePort.Listener listener,
                                        RoutinesRepository.StateChangePort.Callback callback) {
                                    socket.subscribeStateChanges(
                                            listener::onStateChanged,
                                            (subscription, error) -> {
                                                RoutinesRepository.StateChangePort.Subscription mapped =
                                                        subscription == null
                                                                ? null
                                                                : subscription::cancel;
                                                callback.onResult(mapped, error);
                                            });
                                }
                            };
                    RoutinesRepository routinesRepository = new RoutinesRepository(
                            socket::send,
                            routinesStateChangePort);
                    RoutinesController.RepositoryPort routinesRepositoryPort =
                            new RoutinesController.RepositoryPort() {
                                @Override
                                public void loadRoutines(RoutinesRepository.LoadCallback callback) {
                                    try {
                                        routinesRepository.loadRoutines(callback);
                                    } catch (RuntimeException unavailable) {
                                        callback.onResult(null, "Home Assistant is offline.");
                                    }
                                }

                                @Override
                                public RoutinesRepository.Execution run(
                                        RoutineItem routine,
                                        RoutinesRepository.RunCallback callback) {
                                    try {
                                        return routinesRepository.run(routine, callback);
                                    } catch (RuntimeException unavailable) {
                                        callback.onResult(false, "Home Assistant is offline.");
                                        return () -> { };
                                    }
                                }
                            };
                    RoutinesController.SchedulerPort routinesScheduler = (delayMs, runnable) -> {
                        mainHandler.postDelayed(runnable, delayMs);
                        return () -> mainHandler.removeCallbacks(runnable);
                    };
                    closeRoutinesController();
                    routinesController = new RoutinesController(
                            routinesRepositoryPort,
                            routinesScheduler,
                            state -> runOnUiThread(() -> {
                                if (socket != homeSocket || !homeShellVisible
                                        || !sameSelectedRoom(room)
                                        || isFinishing() || isDestroyed()) {
                                    return;
                                }
                                routinesState = state;
                                if (routinesView != null) {
                                    routinesView.render(state);
                                }
                            }));
                    routinesController.start();
                });
            }

            @Override
            public void onOffline(String message) {
                runOnUiThread(() -> {
                    if (socket != homeSocket || !homeShellVisible || !sameSelectedRoom(room)) {
                        return;
                    }
                    if (dashboardController == null) {
                        showOfflineDashboard(room, "Home Assistant is offline.");
                    } else {
                        dashboardController.markOffline("Home Assistant is offline.");
                    }
                    markRoutinesOffline();
                    scheduleHomeDashboardReconnect();
                });
            }

            @Override
            public void onReauthRequired(String message) {
                runOnUiThread(() -> {
                    if (socket != homeSocket || !homeShellVisible || !sameSelectedRoom(room)) {
                        return;
                    }
                    if (dashboardController == null) {
                        showOfflineDashboard(
                                room,
                                "Home Assistant needs BOOP to pair again.");
                    } else {
                        dashboardController.markOffline(
                                "Home Assistant needs BOOP to pair again.");
                    }
                    markRoutinesOffline();
                });
            }
        });
    }

    private HomeDashboardController createDashboardController(
            AreaInfo room,
            HomeDashboardController.RepositoryPort repositoryPort) {
        HomeDashboardController.CachePort cachePort = new HomeDashboardController.CachePort() {
            @Override
            public EntityCard load(AreaInfo selectedRoom) {
                return preferences.cachedFavourite(selectedRoom);
            }

            @Override
            public void save(AreaInfo selectedRoom, EntityCard card) {
                preferences.setCachedFavourite(selectedRoom, card);
            }

            @Override
            public void clear(AreaInfo selectedRoom) {
                preferences.clearCachedFavourite(selectedRoom);
            }
        };

        return new HomeDashboardController(
                room,
                repositoryPort,
                cachePort,
                state -> runOnUiThread(() -> {
                    if (!homeShellVisible || !sameSelectedRoom(room)
                            || isFinishing() || isDestroyed()) {
                        return;
                    }
                    dashboardState = state;
                    if (homeView != null) {
                        homeView.render(state);
                    }
                }));
    }

    private void showOfflineDashboard(AreaInfo room, String message) {
        if (!homeShellVisible || !sameSelectedRoom(room)) {
            return;
        }
        if (dashboardController != null) {
            dashboardController.markOffline(message);
            return;
        }

        HomeDashboardController.RepositoryPort offlinePort =
                new HomeDashboardController.RepositoryPort() {
                    @Override
                    public void loadDashboard(
                            AreaInfo selectedRoom,
                            HomeAssistantRepository.DashboardCallback callback) {
                        callback.onResult(null, message);
                    }

                    @Override
                    public void toggleBinary(
                            EntityCard card,
                            HomeAssistantRepository.BinaryActionCallback callback) {
                        callback.onResult(false, null, "Home Assistant is offline.");
                    }
                };
        dashboardController = createDashboardController(room, offlinePort);
        dashboardController.start();
    }

    private void scheduleHomeDashboardReconnect() {
        if (!homeShellVisible || mainHandler == null || dashboardReconnectRunnable == null) {
            return;
        }
        mainHandler.removeCallbacks(dashboardReconnectRunnable);
        dashboardReconnectAttempt++;
        long delay = Math.min(
                DASHBOARD_RETRY_MAX_MS,
                DASHBOARD_RETRY_MIN_MS * Math.max(1, dashboardReconnectAttempt));
        mainHandler.postDelayed(dashboardReconnectRunnable, delay);
    }

    private boolean sameSelectedRoom(AreaInfo expected) {
        if (expected == null || preferences == null) {
            return false;
        }
        AreaInfo selected = preferences.selectedRoom();
        return selected != null && expected.id().equals(selected.id());
    }

    private FocusCardView createRailCard(String label, TvNavigationModel.Page page) {
        FocusCardView card = new FocusCardView(this).label(label);
        card.setOnClickListener(view -> {
            if (navigationModel == null) {
                return;
            }
            navigationModel.selectRail(page);
            renderNavigationPage(page, true);
        });
        card.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN
                    || keyCode != KeyEvent.KEYCODE_DPAD_RIGHT
                    || navigationModel == null) {
                return false;
            }
            navigationModel.selectRail(page);
            renderNavigationPage(page, false);
            navigationModel.enterContent();
            if (currentPageFirstFocusable != null) {
                currentPageFirstFocusable.requestFocus();
            }
            return true;
        });
        return card;
    }

    private LinearLayout.LayoutParams railCardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(12);
        return params;
    }

    private void renderNavigationPage(TvNavigationModel.Page page, boolean focusRail) {
        if (!homeShellVisible || navigationModel == null || navigationContent == null) {
            return;
        }

        AreaInfo room = preferences == null ? null : preferences.selectedRoom();
        Runnable onContentLeft = this::returnContentFocusToRail;
        View pageView;

        if (page == TvNavigationModel.Page.ROUTINES) {
            homeView = null;
            routinesView = new TvRoutinesView(this, new TvRoutinesView.Listener() {
                @Override
                public void onRun(String entityId) {
                    if (routinesController != null) {
                        routinesController.runRoutine(entityId);
                    }
                }

                @Override
                public void onContentLeft() {
                    returnContentFocusToRail();
                }
            });
            if (routinesState != null) {
                routinesView.render(routinesState);
            }
            pageView = routinesView;
            currentPageFirstFocusable = routinesView.firstFocusable();
        } else if (page == TvNavigationModel.Page.SETTINGS) {
            homeView = null;
            routinesView = null;
            TvSettingsView settingsView = new TvSettingsView(
                    this,
                    room,
                    this::showRoomPicker,
                    onContentLeft);
            pageView = settingsView;
            currentPageFirstFocusable = settingsView.firstFocusable();
        } else {
            routinesView = null;
            homeView = new TvHomeView(
                    this,
                    room,
                    onContentLeft,
                    () -> {
                        if (dashboardController != null) {
                            dashboardController.toggleFavourite();
                        }
                    });
            if (dashboardState != null) {
                homeView.render(dashboardState);
            }
            pageView = homeView;
            currentPageFirstFocusable = homeView.firstFocusable();
        }

        navigationContent.removeAllViews();
        navigationContent.addView(pageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        updateRailVisuals();

        if (focusRail) {
            View railCard = railCardFor(navigationModel.page());
            if (railCard != null) {
                railCard.post(railCard::requestFocus);
            }
        }
    }

    private void returnContentFocusToRail() {
        if (navigationModel == null) {
            return;
        }
        navigationModel.onContentLeft();
        View railCard = railCardFor(navigationModel.page());
        if (railCard != null) {
            railCard.requestFocus();
        }
    }

    private View railCardFor(TvNavigationModel.Page page) {
        if (page == TvNavigationModel.Page.ROUTINES) {
            return routinesRailCard;
        }
        if (page == TvNavigationModel.Page.SETTINGS) {
            return settingsRailCard;
        }
        return homeRailCard;
    }

    private void updateRailVisuals() {
        if (navigationModel == null) {
            return;
        }
        TvNavigationModel.Page page = navigationModel.page();
        if (homeRailCard != null) {
            homeRailCard.setActivatedVisual(page == TvNavigationModel.Page.HOME);
        }
        if (routinesRailCard != null) {
            routinesRailCard.setActivatedVisual(page == TvNavigationModel.Page.ROUTINES);
        }
        if (settingsRailCard != null) {
            settingsRailCard.setActivatedVisual(page == TvNavigationModel.Page.SETTINGS);
        }
    }

    private void clearNavigationShellState() {
        homeShellVisible = false;
        if (mainHandler != null && dashboardReconnectRunnable != null) {
            mainHandler.removeCallbacks(dashboardReconnectRunnable);
        }
        dashboardReconnectAttempt = 0;
        dashboardController = null;
        dashboardState = null;
        homeView = null;
        closeRoutinesController();
        routinesState = null;
        routinesView = null;
        navigationModel = null;
        navigationContent = null;
        homeRailCard = null;
        routinesRailCard = null;
        settingsRailCard = null;
        currentPageFirstFocusable = null;
    }

    private void markRoutinesOffline() {
        if (routinesController != null) {
            routinesController.markOffline();
            return;
        }
        routinesState = RoutinesController.ViewState.offline();
        if (routinesView != null) {
            routinesView.render(routinesState);
        }
    }

    private void closeRoutinesController() {
        RoutinesController controller = routinesController;
        routinesController = null;
        if (controller != null) {
            controller.close();
        }
    }

    private void restartPairing() {
        mainHandler.removeCallbacks(expiryRunnable);
        mainHandler.removeCallbacks(successRunnable);
        PairingGateController controller = pairingController;
        if (controller != null) {
            controller.start();
        }
    }

    private InetAddress activeLanAddress() {
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        if (manager == null) {
            throw new IllegalStateException("Network manager unavailable");
        }

        Network active = manager.getActiveNetwork();
        InetAddress activeAddress = usableAddress(manager, active);
        if (activeAddress != null && isPhysicalLan(manager, active)) {
            return activeAddress;
        }

        for (Network network : manager.getAllNetworks()) {
            if (!isPhysicalLan(manager, network)) {
                continue;
            }
            InetAddress address = usableAddress(manager, network);
            if (address != null) {
                return address;
            }
        }
        throw new IllegalStateException("No LAN address available");
    }

    private static boolean isPhysicalLan(ConnectivityManager manager, Network network) {
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
        if (capabilities == null || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return false;
        }
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    private static InetAddress usableAddress(ConnectivityManager manager, Network network) {
        if (network == null) {
            return null;
        }
        LinkProperties properties = manager.getLinkProperties(network);
        if (properties == null) {
            return null;
        }

        InetAddress fallback = null;
        for (LinkAddress linkAddress : properties.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (address == null
                    || address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isMulticastAddress()) {
                continue;
            }
            if (address instanceof Inet4Address) {
                return address;
            }
            if (fallback == null) {
                fallback = address;
            }
        }
        return fallback;
    }

    private int pairingQrSizePx() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        return Math.max(280, Math.min(width, height) * 52 / 100);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}

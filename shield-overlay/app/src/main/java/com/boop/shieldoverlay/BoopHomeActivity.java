package com.boop.shieldoverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public final class BoopHomeActivity extends Activity {
    private static final long FOUND_IT_BEAT_MS = 700L;

    private final FirstRunCoordinator firstRun = new FirstRunCoordinator();

    private Handler mainHandler;
    private ExecutorService pairingExecutor;
    private ExecutorService homeExecutor;
    private PairingGateController pairingController;
    private HomeAssistantWebSocket homeSocket;
    private BoopPreferences preferences;
    private Runnable expiryRunnable;
    private Runnable successRunnable;
    private TvPairingView pairingView;
    private TvRoomPickerView roomPickerView;

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
        }
        if (pairingController != null) {
            pairingController.close();
            pairingController = null;
        }
        closeHomeSocket();
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
            showHomePlaceholder();
        }
    }

    private void showRoomPicker() {
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
        showHomePlaceholder();
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

    private void showHomePlaceholder() {
        roomPickerView = null;
        LinearLayout root = simpleStage();
        AreaInfo room = preferences == null ? null : preferences.selectedRoom();
        root.addView(stageTitle(getString(R.string.home_title)));
        if (room != null) {
            root.addView(stageDetail(room.name()));
        }
        setContentView(root);
        pairingView = null;
    }

    private LinearLayout simpleStage() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(64), dp(48), dp(64), dp(48));
        root.setBackgroundColor(Color.BLACK);
        return root;
    }

    private TextView stageTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(52f);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER);
        return view;
    }

    private TextView stageDetail(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.LTGRAY);
        view.setTextSize(26f);
        view.setGravity(Gravity.CENTER);
        view.setPadding(0, dp(18), 0, 0);
        return view;
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

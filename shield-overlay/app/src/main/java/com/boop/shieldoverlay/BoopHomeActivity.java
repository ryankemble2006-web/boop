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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public final class BoopHomeActivity extends Activity {
    private Handler mainHandler;
    private ExecutorService pairingExecutor;
    private PairingGateController pairingController;
    private Runnable expiryRunnable;

    private TextView titleView;
    private TextView statusView;
    private ImageView qrView;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepAwakeAndHideSystemUi();

        mainHandler = new Handler(Looper.getMainLooper());
        expiryRunnable = () -> {
            PairingGateController controller = pairingController;
            if (controller != null) {
                controller.expireIfNeeded();
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
        if (mainHandler != null && expiryRunnable != null) {
            mainHandler.removeCallbacks(expiryRunnable);
        }
        if (pairingController != null) {
            pairingController.close();
            pairingController = null;
        }
        if (pairingExecutor != null) {
            pairingExecutor.shutdownNow();
            pairingExecutor = null;
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
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(48), dp(32), dp(48), dp(32));
        root.setBackgroundColor(Color.BLACK);

        titleView = new TextView(this);
        titleView.setText(R.string.pairing_title_searching);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(42f);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        root.addView(titleView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        int qrSize = pairingQrSizePx();
        qrView = new ImageView(this);
        qrView.setContentDescription(getString(R.string.pairing_scan));
        qrView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qrView.setVisibility(View.GONE);
        LinearLayout.LayoutParams qrParams = new LinearLayout.LayoutParams(qrSize, qrSize);
        qrParams.topMargin = dp(18);
        qrParams.bottomMargin = dp(18);
        root.addView(qrView, qrParams);

        statusView = new TextView(this);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(24f);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(4), 0, dp(12));
        root.addView(statusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        retryButton = new Button(this);
        retryButton.setText(R.string.pairing_retry);
        retryButton.setTextSize(24f);
        retryButton.setAllCaps(false);
        retryButton.setFocusable(true);
        retryButton.setMinHeight(dp(68));
        retryButton.setPadding(dp(36), dp(12), dp(36), dp(12));
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackgroundColor(Color.DKGRAY);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(view -> restartPairing());
        retryButton.setOnFocusChangeListener((view, hasFocus) -> {
            retryButton.setBackgroundColor(hasFocus ? Color.WHITE : Color.DKGRAY);
            retryButton.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        });
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        retryParams.topMargin = dp(10);
        root.addView(retryButton, retryParams);

        setContentView(root, new ViewGroup.LayoutParams(
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
        if (state == null || isFinishing() || isDestroyed()) {
            return;
        }
        mainHandler.removeCallbacks(expiryRunnable);

        PairingGateController.State value = state.state();
        if (value == PairingGateController.State.SEARCHING) {
            titleView.setText(R.string.pairing_title_searching);
            statusView.setText(state.message());
            hideQrAndRetry();
            return;
        }

        if (value == PairingGateController.State.QR_READY) {
            titleView.setText(R.string.pairing_title_found);
            statusView.setText(R.string.pairing_scan);
            retryButton.setVisibility(View.GONE);
            try {
                qrView.setImageBitmap(QrCodeBitmap.render(
                        state.qrPayload(),
                        pairingQrSizePx()));
                qrView.setVisibility(View.VISIBLE);
                long delayMs = Math.max(1L, state.expiresAtMs() - System.currentTimeMillis());
                mainHandler.postDelayed(expiryRunnable, delayMs);
            } catch (IllegalArgumentException couldNotDrawQr) {
                pairingController.close();
                qrView.setVisibility(View.GONE);
                showRetryState(getString(R.string.pairing_retry));
            }
            return;
        }

        if (value == PairingGateController.State.AUTHORIZING) {
            titleView.setText(R.string.pairing_title_found);
            statusView.setText(R.string.pairing_authorizing);
            hideQrAndRetry();
            return;
        }

        if (value == PairingGateController.State.CONNECTED) {
            titleView.setText(R.string.pairing_found);
            statusView.setText("");
            hideQrAndRetry();
            return;
        }

        if (value == PairingGateController.State.STALE) {
            showRetryState(state.message());
            return;
        }

        if (value == PairingGateController.State.FAILED) {
            showRetryState(state.message());
        }
    }

    private void hideQrAndRetry() {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    private void showRetryState(String message) {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        titleView.setText(message == null || message.trim().isEmpty()
                ? getString(R.string.pairing_retry)
                : message);
        statusView.setText("");
        retryButton.setVisibility(View.VISIBLE);
        retryButton.requestFocus();
    }

    private void restartPairing() {
        mainHandler.removeCallbacks(expiryRunnable);
        retryButton.setVisibility(View.GONE);
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

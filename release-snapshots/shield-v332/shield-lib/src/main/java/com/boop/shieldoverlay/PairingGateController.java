package com.boop.shieldoverlay;

import java.net.InetAddress;
import java.util.concurrent.Executor;

public final class PairingGateController implements AutoCloseable {
    public enum State {
        SEARCHING,
        QR_READY,
        AUTHORIZING,
        CONNECTED,
        STALE,
        FAILED
    }

    public interface Clock {
        long nowMs();
    }

    public interface Listener {
        void onStateChanged(ViewState state);
    }

    public interface AddressProvider {
        InetAddress localAddress() throws Exception;
    }

    public interface DiscoveryPort {
        interface Listener {
            void onDiscovered(DiscoveredHomeAssistant homeAssistant);

            default void onError(String message) {
                // Optional for simple test doubles.
            }
        }

        void start(Listener listener);
        void stop();
    }

    public interface PairingServerPort {
        interface Listener {
            void onAccepted(PairingRelayMessage message);
        }

        ServerEndpoint start(
                InetAddress bindAddress,
                PairingSession session,
                Listener listener) throws Exception;

        void close();
    }

    public interface AuthPort {
        AuthTokenSet exchangeAuthorizationCode(
                String baseUrl,
                String authorizationCode,
                String clientId) throws Exception;

        AuthTokenSet refresh(
                String baseUrl,
                String refreshToken,
                String clientId) throws Exception;
    }

    public interface CredentialStorePort {
        StoredHomeAssistantCredential load() throws Exception;
        void save(StoredHomeAssistantCredential credential) throws Exception;
    }

    public static final class ServerEndpoint {
        private final String host;
        private final int port;
        private final String certificatePinSha256;

        public ServerEndpoint(String host, int port, String certificatePinSha256) {
            this.host = requireText(host, "host");
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("port out of range");
            }
            this.port = port;
            this.certificatePinSha256 = requireText(
                    certificatePinSha256,
                    "certificate pin");
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }

        public String certificatePinSha256() {
            return certificatePinSha256;
        }
    }

    public static final class ViewState {
        private final State state;
        private final String message;
        private final String qrPayload;
        private final long expiresAtMs;

        private ViewState(
                State state,
                String message,
                String qrPayload,
                long expiresAtMs) {
            this.state = state;
            this.message = message;
            this.qrPayload = qrPayload;
            this.expiresAtMs = expiresAtMs;
        }

        public State state() {
            return state;
        }

        public String message() {
            return message;
        }

        public String qrPayload() {
            return qrPayload;
        }

        public long expiresAtMs() {
            return expiresAtMs;
        }
    }

    private final DiscoveryPort discovery;
    private final PairingServerPort pairingServer;
    private final AuthPort auth;
    private final CredentialStorePort credentialStore;
    private final AddressProvider addressProvider;
    private final Executor executor;
    private final Clock clock;
    private final Listener listener;

    private boolean closed;
    private State state;
    private PairingSession pairingSession;
    private DiscoveredHomeAssistant activeHomeAssistant;
    private String inMemoryAccessToken;

    public PairingGateController(
            DiscoveryPort discovery,
            PairingServerPort pairingServer,
            AuthPort auth,
            CredentialStorePort credentialStore,
            AddressProvider addressProvider,
            Executor executor,
            Clock clock,
            Listener listener) {
        if (discovery == null
                || pairingServer == null
                || auth == null
                || credentialStore == null
                || addressProvider == null
                || executor == null
                || clock == null
                || listener == null) {
            throw new IllegalArgumentException("pairing controller dependencies are required");
        }
        this.discovery = discovery;
        this.pairingServer = pairingServer;
        this.auth = auth;
        this.credentialStore = credentialStore;
        this.addressProvider = addressProvider;
        this.executor = executor;
        this.clock = clock;
        this.listener = listener;
    }

    public void start() {
        synchronized (this) {
            if (closed) {
                closed = false;
            }
            pairingSession = null;
            activeHomeAssistant = null;
            pairingServer.close();
            discovery.stop();
        }

        final StoredHomeAssistantCredential stored;
        try {
            stored = credentialStore.load();
        } catch (Exception e) {
            fail("I couldn't open BOOP's house key. Try pairing again.");
            return;
        }

        if (stored != null) {
            emit(State.AUTHORIZING, "Checking the house…", null, 0L);
            executor.execute(() -> refreshStoredCredential(stored));
            return;
        }

        emit(State.SEARCHING, "Looking for your house…", null, 0L);
        try {
            discovery.start(new DiscoveryPort.Listener() {
                @Override
                public void onDiscovered(DiscoveredHomeAssistant homeAssistant) {
                    handleDiscovered(homeAssistant);
                }

                @Override
                public void onError(String message) {
                    fail(message == null || message.trim().isEmpty()
                            ? "I couldn't find Home Assistant on this network."
                            : message);
                }
            });
        } catch (RuntimeException e) {
            fail("I couldn't look for Home Assistant on this network.");
        }
    }

    public boolean expireIfNeeded() {
        synchronized (this) {
            if (closed || state != State.QR_READY || pairingSession == null) {
                return false;
            }
            if (pairingSession.isActive(clock.nowMs())) {
                return false;
            }
            pairingServer.close();
            pairingSession = null;
            activeHomeAssistant = null;
        }
        emit(State.STALE, "That one went stale.", null, 0L);
        return true;
    }

    public synchronized String inMemoryAccessToken() {
        return inMemoryAccessToken;
    }

    @Override
    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            pairingSession = null;
            activeHomeAssistant = null;
            inMemoryAccessToken = null;
        }
        discovery.stop();
        pairingServer.close();
    }

    private void handleDiscovered(DiscoveredHomeAssistant homeAssistant) {
        if (homeAssistant == null) {
            fail("I found Home Assistant, but its address wasn't usable.");
            return;
        }

        synchronized (this) {
            if (closed || state != State.SEARCHING) {
                return;
            }
        }
        discovery.stop();

        PairingSession session = PairingSession.newSession(clock.nowMs());
        try {
            InetAddress bindAddress = addressProvider.localAddress();
            ServerEndpoint endpoint = pairingServer.start(
                    bindAddress,
                    session,
                    this::handleAcceptedRelay);

            PairingQrPayload payload = new PairingQrPayload(
                    endpoint.host(),
                    endpoint.port(),
                    session.sessionId(),
                    session.secret(),
                    endpoint.certificatePinSha256(),
                    homeAssistant.baseUrl());

            synchronized (this) {
                if (closed) {
                    pairingServer.close();
                    return;
                }
                pairingSession = session;
                activeHomeAssistant = homeAssistant;
            }
            emit(
                    State.QR_READY,
                    "Scan this with BOOP Wall.",
                    payload.toUri().toString(),
                    session.expiresAtMs());
        } catch (Exception e) {
            pairingServer.close();
            fail("I found your house, but couldn't open a secure pairing link.");
        }
    }

    private void handleAcceptedRelay(PairingRelayMessage message) {
        final DiscoveredHomeAssistant homeAssistant;
        synchronized (this) {
            if (closed || state != State.QR_READY || message == null) {
                return;
            }
            homeAssistant = activeHomeAssistant;
            pairingSession = null;
        }
        pairingServer.close();
        if (homeAssistant == null) {
            fail("That pairing request went stale. Try again.");
            return;
        }

        emit(State.AUTHORIZING, "Home Assistant is approving BOOP…", null, 0L);
        executor.execute(() -> exchangeAndStore(homeAssistant, message));
    }

    private void exchangeAndStore(
            DiscoveredHomeAssistant homeAssistant,
            PairingRelayMessage message) {
        try {
            AuthTokenSet tokens = auth.exchangeAuthorizationCode(
                    homeAssistant.baseUrl(),
                    message.authorizationCode(),
                    message.clientId());
            String refreshToken = tokens.refreshToken();
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalStateException("refresh credential missing");
            }

            credentialStore.save(new StoredHomeAssistantCredential(
                    homeAssistant.baseUrl(),
                    message.clientId(),
                    refreshToken));

            synchronized (this) {
                if (closed) {
                    return;
                }
                inMemoryAccessToken = tokens.accessToken();
            }
            emit(State.CONNECTED, "Found it.", null, 0L);
        } catch (Exception e) {
            fail("Home Assistant didn't finish pairing. Try again.");
        }
    }

    private void refreshStoredCredential(StoredHomeAssistantCredential stored) {
        try {
            AuthTokenSet tokens = auth.refresh(
                    stored.baseUrl(),
                    stored.refreshToken(),
                    stored.clientId());
            synchronized (this) {
                if (closed) {
                    return;
                }
                inMemoryAccessToken = tokens.accessToken();
            }
            emit(State.CONNECTED, "Found it.", null, 0L);
        } catch (Exception e) {
            fail("I couldn't reach the house right now.");
        }
    }

    private void fail(String message) {
        discovery.stop();
        pairingServer.close();
        synchronized (this) {
            if (closed) {
                return;
            }
            pairingSession = null;
            activeHomeAssistant = null;
        }
        emit(State.FAILED, message, null, 0L);
    }

    private void emit(State newState, String message, String qrPayload, long expiresAtMs) {
        ViewState viewState;
        synchronized (this) {
            if (closed) {
                return;
            }
            state = newState;
            viewState = new ViewState(newState, message, qrPayload, expiresAtMs);
        }
        listener.onStateChanged(viewState);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}

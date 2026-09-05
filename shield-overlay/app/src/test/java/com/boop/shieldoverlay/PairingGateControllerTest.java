package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;

public final class PairingGateControllerTest {
    private FakeDiscovery discovery;
    private FakePairingServer server;
    private FakeAuth auth;
    private FakeStore store;
    private FakeClock clock;
    private FakeListener listener;
    private PairingGateController controller;

    @Before
    public void setUp() throws Exception {
        discovery = new FakeDiscovery();
        server = new FakePairingServer();
        auth = new FakeAuth();
        store = new FakeStore();
        clock = new FakeClock(1_000L);
        listener = new FakeListener();
        Executor direct = Runnable::run;

        controller = new PairingGateController(
                discovery,
                server,
                auth,
                store,
                () -> InetAddress.getByName("192.168.1.20"),
                direct,
                clock,
                listener);
    }

    @Test
    public void discoveryCreatesOneQrUsingCurrentTemporaryCertificatePin() throws Exception {
        controller.start();
        assertEquals(PairingGateController.State.SEARCHING, listener.latest.state());

        discovery.found(new DiscoveredHomeAssistant(
                "Home",
                "ha-uuid",
                "http://homeassistant.local:8123"));

        assertEquals(1, server.startCount);
        assertEquals(PairingGateController.State.QR_READY, listener.latest.state());
        assertNotNull(listener.latest.qrPayload());

        PairingQrPayload payload = PairingQrPayload.parse(URI.create(listener.latest.qrPayload()));
        assertEquals("192.168.1.20", payload.host());
        assertEquals(42123, payload.port());
        assertEquals("cert-pin-123", payload.certificatePinSha256());
        assertEquals(server.session.sessionId(), payload.sessionId());
        assertEquals(server.session.secret(), payload.secret());
        assertEquals("http://homeassistant.local:8123", payload.homeAssistantBaseUrl());
    }

    @Test
    public void expiryClosesTemporaryListenerAndMarksCodeStale() {
        controller.start();
        discovery.found(new DiscoveredHomeAssistant("Home", "id", "http://ha.local:8123"));

        clock.now = server.session.expiresAtMs();
        assertTrue(controller.expireIfNeeded());

        assertTrue(server.closed);
        assertEquals(PairingGateController.State.STALE, listener.latest.state());
        assertNull(listener.latest.qrPayload());
    }

    @Test
    public void acceptedRelayUsesRelayedClientIdAndStoresOnlyShieldRefreshCredential() {
        controller.start();
        discovery.found(new DiscoveredHomeAssistant("Home", "id", "http://ha.local:8123"));

        auth.exchangeResult = new AuthTokenSet("access-1", "refresh-1", 1800L, "Bearer");
        server.accept(new PairingRelayMessage("one-time-code", "http://127.0.0.1:43123/"));

        assertEquals("http://ha.local:8123", auth.exchangeBaseUrl);
        assertEquals("one-time-code", auth.exchangeCode);
        assertEquals("http://127.0.0.1:43123/", auth.exchangeClientId);
        assertNotNull(store.saved);
        assertEquals("http://ha.local:8123", store.saved.baseUrl());
        assertEquals("http://127.0.0.1:43123/", store.saved.clientId());
        assertEquals("refresh-1", store.saved.refreshToken());
        assertEquals(PairingGateController.State.CONNECTED, listener.latest.state());
    }

    @Test
    public void existingCredentialRefreshesWithoutStartingDiscoveryOrQr() {
        store.loaded = new StoredHomeAssistantCredential(
                "http://ha.local:8123",
                "http://127.0.0.1:43123/",
                "refresh-1");
        auth.refreshResult = new AuthTokenSet("access-2", null, 1800L, "Bearer");

        controller.start();

        assertEquals(0, discovery.startCount);
        assertEquals(0, server.startCount);
        assertEquals("refresh-1", auth.refreshToken);
        assertEquals("http://127.0.0.1:43123/", auth.refreshClientId);
        assertEquals(PairingGateController.State.CONNECTED, listener.latest.state());
    }

    @Test
    public void cancelStopsDiscoveryAndTemporaryListener() {
        controller.start();
        discovery.found(new DiscoveredHomeAssistant("Home", "id", "http://ha.local:8123"));

        controller.close();

        assertTrue(discovery.stopped);
        assertTrue(server.closed);
    }

    private static final class FakeClock implements PairingGateController.Clock {
        long now;
        FakeClock(long now) { this.now = now; }
        @Override public long nowMs() { return now; }
    }

    private static final class FakeListener implements PairingGateController.Listener {
        PairingGateController.ViewState latest;
        @Override public void onStateChanged(PairingGateController.ViewState state) {
            latest = state;
        }
    }

    private static final class FakeDiscovery implements PairingGateController.DiscoveryPort {
        PairingGateController.DiscoveryPort.Listener listener;
        int startCount;
        boolean stopped;

        @Override public void start(PairingGateController.DiscoveryPort.Listener listener) {
            this.listener = listener;
            startCount++;
        }

        @Override public void stop() {
            stopped = true;
        }

        void found(DiscoveredHomeAssistant homeAssistant) {
            listener.onDiscovered(homeAssistant);
        }
    }

    private static final class FakePairingServer implements PairingGateController.PairingServerPort {
        PairingGateController.PairingServerPort.Listener listener;
        PairingSession session;
        int startCount;
        boolean closed;

        @Override
        public PairingGateController.ServerEndpoint start(
                InetAddress bindAddress,
                PairingSession session,
                PairingGateController.PairingServerPort.Listener listener) {
            this.session = session;
            this.listener = listener;
            startCount++;
            return new PairingGateController.ServerEndpoint(
                    bindAddress.getHostAddress(), 42123, "cert-pin-123");
        }

        @Override public void close() {
            closed = true;
        }

        void accept(PairingRelayMessage message) {
            listener.onAccepted(message);
        }
    }

    private static final class FakeAuth implements PairingGateController.AuthPort {
        AuthTokenSet exchangeResult;
        AuthTokenSet refreshResult;
        String exchangeBaseUrl;
        String exchangeCode;
        String exchangeClientId;
        String refreshToken;
        String refreshClientId;

        @Override
        public AuthTokenSet exchangeAuthorizationCode(String baseUrl, String code, String clientId) {
            exchangeBaseUrl = baseUrl;
            exchangeCode = code;
            exchangeClientId = clientId;
            return exchangeResult;
        }

        @Override
        public AuthTokenSet refresh(String baseUrl, String refreshToken, String clientId) {
            this.refreshToken = refreshToken;
            refreshClientId = clientId;
            return refreshResult;
        }
    }

    private static final class FakeStore implements PairingGateController.CredentialStorePort {
        StoredHomeAssistantCredential loaded;
        StoredHomeAssistantCredential saved;

        @Override public StoredHomeAssistantCredential load() {
            return loaded;
        }

        @Override public void save(StoredHomeAssistantCredential credential) {
            saved = credential;
        }
    }
}

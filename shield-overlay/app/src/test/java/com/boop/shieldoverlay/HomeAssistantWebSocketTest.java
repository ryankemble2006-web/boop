package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class HomeAssistantWebSocketTest {
    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void exposesStateChangeSubscriptionApi() throws Exception {
        Class<?> stateListener = null;
        Class<?> subscriptionCallback = null;
        for (Class<?> nested : HomeAssistantWebSocket.class.getDeclaredClasses()) {
            if ("StateChangeListener".equals(nested.getSimpleName())) {
                stateListener = nested;
            } else if ("SubscriptionCallback".equals(nested.getSimpleName())) {
                subscriptionCallback = nested;
            }
        }

        assertNotNull("socket needs a state-change listener type", stateListener);
        assertNotNull("socket needs a subscription callback type", subscriptionCallback);
        Method method = HomeAssistantWebSocket.class.getMethod(
                "subscribeStateChanges",
                stateListener,
                subscriptionCallback);
        assertNotNull(method);
    }

    @Test
    public void stateChangeSubscriptionDeliversEventAndUnsubscribes() throws Exception {
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch subscribeSeen = new CountDownLatch(1);
        CountDownLatch subscribed = new CountDownLatch(1);
        CountDownLatch eventSeen = new CountDownLatch(1);
        CountDownLatch unsubscribeSeen = new CountDownLatch(1);
        CountDownLatch serverClosed = new CountDownLatch(1);
        AtomicReference<WebSocket> serverSocket = new AtomicReference<>();
        AtomicReference<JSONObject> subscribeMessage = new AtomicReference<>();
        AtomicReference<JSONObject> unsubscribeMessage = new AtomicReference<>();
        AtomicReference<String> entitySeen = new AtomicReference<>();
        AtomicReference<String> stateSeen = new AtomicReference<>();
        AtomicReference<HomeAssistantWebSocket.Subscription> active = new AtomicReference<>();
        AtomicReference<String> subscriptionError = new AtomicReference<>();

        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                serverSocket.set(webSocket);
                webSocket.send("{\"type\":\"auth_required\",\"ha_version\":\"2026.9\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    JSONObject message = new JSONObject(text);
                    String type = message.optString("type", "");
                    if ("auth".equals(type)) {
                        webSocket.send("{\"type\":\"auth_ok\",\"ha_version\":\"2026.9\"}");
                        return;
                    }
                    if ("subscribe_events".equals(type)) {
                        subscribeMessage.set(message);
                        subscribeSeen.countDown();
                        webSocket.send(new JSONObject()
                                .put("id", message.getInt("id"))
                                .put("type", "result")
                                .put("success", true)
                                .put("result", JSONObject.NULL)
                                .toString());
                        return;
                    }
                    if ("unsubscribe_events".equals(type)) {
                        unsubscribeMessage.set(message);
                        unsubscribeSeen.countDown();
                        webSocket.send(new JSONObject()
                                .put("id", message.getInt("id"))
                                .put("type", "result")
                                .put("success", true)
                                .put("result", JSONObject.NULL)
                                .toString());
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                serverClosed.countDown();
            }
        }));

        HomeAssistantWebSocket socket = new HomeAssistantWebSocket(new OkHttpClient());
        socket.connect(server.url("/").toString(), "access-secret", new HomeAssistantWebSocket.Listener() {
            @Override public void onReady() { ready.countDown(); }
            @Override public void onOffline(String message) { }
            @Override public void onReauthRequired(String message) { }
        });
        assertTrue(ready.await(3, TimeUnit.SECONDS));

        socket.subscribeStateChanges(
                (entityId, state) -> {
                    entitySeen.set(entityId);
                    stateSeen.set(state);
                    eventSeen.countDown();
                },
                (subscription, error) -> {
                    active.set(subscription);
                    subscriptionError.set(error);
                    subscribed.countDown();
                });

        assertTrue("subscribe_events must be sent", subscribeSeen.await(3, TimeUnit.SECONDS));
        JSONObject subscribe = subscribeMessage.get();
        assertEquals("state_changed", subscribe.getString("event_type"));
        int subscriptionId = subscribe.getInt("id");
        assertTrue("subscription must be acknowledged", subscribed.await(3, TimeUnit.SECONDS));
        assertNotNull(active.get());
        assertNull(subscriptionError.get());

        serverSocket.get().send(new JSONObject()
                .put("id", subscriptionId)
                .put("type", "event")
                .put("event", new JSONObject()
                        .put("event_type", "state_changed")
                        .put("data", new JSONObject()
                                .put("entity_id", "switch.sync_box")
                                .put("new_state", new JSONObject().put("state", "off"))))
                .toString());

        assertTrue("state_changed event must reach BOOP", eventSeen.await(3, TimeUnit.SECONDS));
        assertEquals("switch.sync_box", entitySeen.get());
        assertEquals("off", stateSeen.get());

        active.get().cancel();
        assertTrue("unsubscribe_events must be sent", unsubscribeSeen.await(3, TimeUnit.SECONDS));
        JSONObject unsubscribe = unsubscribeMessage.get();
        assertEquals(subscriptionId, unsubscribe.getInt("subscription"));
        assertTrue(unsubscribe.getInt("id") > subscriptionId);

        socket.close();
        assertTrue(serverClosed.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void authenticatesThenUsesMonotonicCommandIds() throws Exception {
        CountDownLatch authSeen = new CountDownLatch(1);
        CountDownLatch commandsSeen = new CountDownLatch(2);
        CountDownLatch serverClosed = new CountDownLatch(1);
        AtomicReference<String> authMessage = new AtomicReference<>();
        AtomicReference<String> firstCommand = new AtomicReference<>();
        AtomicReference<String> secondCommand = new AtomicReference<>();

        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            int commandIndex;

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send("{\"type\":\"auth_required\",\"ha_version\":\"2026.9\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.contains("\"type\":\"auth\"")) {
                    authMessage.set(text);
                    authSeen.countDown();
                    webSocket.send("{\"type\":\"auth_ok\",\"ha_version\":\"2026.9\"}");
                    return;
                }
                if (commandIndex++ == 0) {
                    firstCommand.set(text);
                } else {
                    secondCommand.set(text);
                }
                commandsSeen.countDown();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(code, reason);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                serverClosed.countDown();
            }
        }));

        CountDownLatch ready = new CountDownLatch(1);
        HomeAssistantWebSocket socket = new HomeAssistantWebSocket(new OkHttpClient());
        socket.connect(server.url("/").toString(), "access-secret", new HomeAssistantWebSocket.Listener() {
            @Override public void onReady() { ready.countDown(); }
            @Override public void onOffline(String message) { }
            @Override public void onReauthRequired(String message) { }
        });

        assertTrue(authSeen.await(3, TimeUnit.SECONDS));
        assertTrue(authMessage.get().contains("\"access_token\":\"access-secret\""));
        assertTrue(ready.await(3, TimeUnit.SECONDS));

        socket.send("config/area_registry/list", new JSONObject(), (success, result, error) -> { });
        socket.send("get_states", new JSONObject(), (success, result, error) -> { });

        assertTrue(commandsSeen.await(3, TimeUnit.SECONDS));
        JSONObject first = new JSONObject(firstCommand.get());
        JSONObject second = new JSONObject(secondCommand.get());
        assertEquals(1, first.getInt("id"));
        assertEquals(2, second.getInt("id"));
        assertEquals("config/area_registry/list", first.getString("type"));
        assertEquals("get_states", second.getString("type"));
        socket.close();
        assertTrue(serverClosed.await(3, TimeUnit.SECONDS));
    }

    @Test
    public void authInvalidIsReauthFailureNotReady() throws Exception {
        CountDownLatch serverClosed = new CountDownLatch(1);
        server.enqueue(new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                webSocket.send("{\"type\":\"auth_required\"}");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                webSocket.send("{\"type\":\"auth_invalid\",\"message\":\"Invalid access token\"}");
                webSocket.close(1000, "test complete");
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                serverClosed.countDown();
            }
        }));

        CountDownLatch reauth = new CountDownLatch(1);
        CountDownLatch ready = new CountDownLatch(1);
        HomeAssistantWebSocket socket = new HomeAssistantWebSocket(new OkHttpClient());
        socket.connect(server.url("/").toString(), "bad-token", new HomeAssistantWebSocket.Listener() {
            @Override public void onReady() { ready.countDown(); }
            @Override public void onOffline(String message) { }
            @Override public void onReauthRequired(String message) { reauth.countDown(); }
        });

        assertTrue(reauth.await(3, TimeUnit.SECONDS));
        assertFalse(ready.await(200, TimeUnit.MILLISECONDS));
        socket.close();
        assertTrue(serverClosed.await(3, TimeUnit.SECONDS));
    }
}

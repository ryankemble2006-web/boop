package com.boop.shieldoverlay;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class HomeAssistantWebSocket implements AutoCloseable {
    public interface Listener {
        void onReady();
        void onOffline(String message);
        void onReauthRequired(String message);
    }

    public interface Callback {
        void onResult(boolean success, Object result, String error);
    }

    public interface StateChangeListener {
        void onStateChanged(String entityId, String state);
    }

    public interface AutomationTriggerListener {
        void onTriggered(String entityId);
    }

    public interface Subscription {
        void cancel();
    }

    public interface SubscriptionCallback {
        void onResult(Subscription subscription, String error);
    }

    private final OkHttpClient client;
    private final Object lock = new Object();
    private final Map<Integer, Callback> callbacks = new HashMap<>();
    private final Map<Integer, StateChangeListener> stateChangeSubscriptions = new HashMap<>();
    private final Map<Integer, AutomationTriggerListener> automationTriggerSubscriptions =
            new HashMap<>();

    private WebSocket webSocket;
    private Listener listener;
    private String accessToken;
    private int nextId = 1;
    private boolean ready;
    private boolean deliberateClose;

    public HomeAssistantWebSocket(OkHttpClient client) {
        if (client == null) {
            throw new IllegalArgumentException("HTTP client is required");
        }
        this.client = client;
    }

    public void connect(String baseUrl, String accessToken, Listener listener) {
        String token = requireText(accessToken, "access token");
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }

        final String webSocketUrl = toWebSocketUrl(baseUrl);
        final WebSocket previous;
        synchronized (lock) {
            previous = webSocket;
            webSocket = null;
            callbacks.clear();
            stateChangeSubscriptions.clear();
            automationTriggerSubscriptions.clear();
            nextId = 1;
            ready = false;
            deliberateClose = false;
            this.listener = listener;
            this.accessToken = token;
        }
        if (previous != null) {
            previous.cancel();
        }

        Request request = new Request.Builder().url(webSocketUrl).build();
        WebSocket opened = client.newWebSocket(request, new SocketListener());
        synchronized (lock) {
            webSocket = opened;
        }
    }

    public void send(String type, JSONObject body, Callback callback) {
        final WebSocket socket;
        final int id;
        final String payload;
        synchronized (lock) {
            if (!ready || webSocket == null) {
                throw new IllegalStateException("Home Assistant socket is not ready");
            }
            id = nextId++;
            try {
                payload = new HaCommand(id, type, body).toJson().toString();
            } catch (JSONException e) {
                throw new IllegalArgumentException("Home Assistant command could not be encoded", e);
            }
            if (callback != null) {
                callbacks.put(id, callback);
            }
            socket = webSocket;
        }

        if (!socket.send(payload)) {
            Callback failed;
            synchronized (lock) {
                failed = callbacks.remove(id);
            }
            if (failed != null) {
                failed.onResult(false, null, "Home Assistant connection is unavailable");
            }
        }
    }

    public void subscribeStateChanges(
            StateChangeListener listener,
            SubscriptionCallback callback) {
        if (listener == null) {
            throw new IllegalArgumentException("state-change listener is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("subscription callback is required");
        }

        final WebSocket socket;
        final int subscriptionId;
        final String payload;
        synchronized (lock) {
            if (!ready || webSocket == null) {
                throw new IllegalStateException("Home Assistant socket is not ready");
            }
            subscriptionId = nextId++;
            try {
                payload = new HaCommand(
                        subscriptionId,
                        "subscribe_events",
                        new JSONObject().put("event_type", "state_changed"))
                        .toJson()
                        .toString();
            } catch (JSONException impossible) {
                throw new IllegalArgumentException("Home Assistant subscription could not be encoded", impossible);
            }
            callbacks.put(subscriptionId, (success, result, error) -> {
                if (!success) {
                    callback.onResult(null, error);
                    return;
                }

                boolean active;
                synchronized (lock) {
                    active = ready && webSocket != null && !deliberateClose;
                    if (active) {
                        stateChangeSubscriptions.put(subscriptionId, listener);
                    }
                }
                if (!active) {
                    callback.onResult(null, "Home Assistant connection is unavailable");
                    return;
                }
                callback.onResult(new ActiveSubscription(subscriptionId), null);
            });
            socket = webSocket;
        }

        if (!socket.send(payload)) {
            Callback failed;
            synchronized (lock) {
                failed = callbacks.remove(subscriptionId);
            }
            if (failed != null) {
                failed.onResult(false, null, "Home Assistant connection is unavailable");
            }
        }
    }

    public void subscribeAutomationTriggers(
            AutomationTriggerListener listener,
            SubscriptionCallback callback) {
        if (listener == null) {
            throw new IllegalArgumentException("automation-trigger listener is required");
        }
        if (callback == null) {
            throw new IllegalArgumentException("subscription callback is required");
        }

        final WebSocket socket;
        final int subscriptionId;
        final String payload;
        synchronized (lock) {
            if (!ready || webSocket == null) {
                throw new IllegalStateException("Home Assistant socket is not ready");
            }
            subscriptionId = nextId++;
            try {
                payload = new HaCommand(
                        subscriptionId,
                        "subscribe_events",
                        new JSONObject().put("event_type", "automation_triggered"))
                        .toJson()
                        .toString();
            } catch (JSONException impossible) {
                throw new IllegalArgumentException(
                        "Home Assistant subscription could not be encoded", impossible);
            }
            callbacks.put(subscriptionId, (success, result, error) -> {
                if (!success) {
                    callback.onResult(null, error);
                    return;
                }

                boolean active;
                synchronized (lock) {
                    active = ready && webSocket != null && !deliberateClose;
                    if (active) {
                        automationTriggerSubscriptions.put(subscriptionId, listener);
                    }
                }
                if (!active) {
                    callback.onResult(null, "Home Assistant connection is unavailable");
                    return;
                }
                callback.onResult(new ActiveSubscription(subscriptionId), null);
            });
            socket = webSocket;
        }

        if (!socket.send(payload)) {
            Callback failed;
            synchronized (lock) {
                failed = callbacks.remove(subscriptionId);
            }
            if (failed != null) {
                failed.onResult(false, null, "Home Assistant connection is unavailable");
            }
        }
    }

    public boolean isReady() {
        synchronized (lock) {
            return ready;
        }
    }

    @Override
    public void close() {
        final WebSocket socket;
        final Map<Integer, Callback> pending;
        synchronized (lock) {
            deliberateClose = true;
            ready = false;
            accessToken = null;
            listener = null;
            socket = webSocket;
            webSocket = null;
            pending = new HashMap<>(callbacks);
            callbacks.clear();
            stateChangeSubscriptions.clear();
            automationTriggerSubscriptions.clear();
        }
        if (socket != null) {
            socket.close(1000, "BOOP Home closed");
        }
        for (Callback callback : pending.values()) {
            callback.onResult(false, null, "Home Assistant connection closed");
        }
    }

    static String toWebSocketUrl(String baseUrl) {
        String text = requireText(baseUrl, "Home Assistant URL").trim();
        final URI uri;
        try {
            uri = URI.create(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid Home Assistant URL", e);
        }

        String scheme = uri.getScheme();
        final String webSocketScheme;
        if ("http".equalsIgnoreCase(scheme) || "ws".equalsIgnoreCase(scheme)) {
            webSocketScheme = "ws";
        } else if ("https".equalsIgnoreCase(scheme) || "wss".equalsIgnoreCase(scheme)) {
            webSocketScheme = "wss";
        } else {
            throw new IllegalArgumentException("Home Assistant URL must be HTTP or HTTPS");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("Home Assistant URL has no host");
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "/api/websocket";
        } else {
            while (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            if (!path.endsWith("/api/websocket")) {
                path = path + "/api/websocket";
            }
        }

        try {
            return new URI(
                    webSocketScheme,
                    uri.getUserInfo(),
                    uri.getHost(),
                    uri.getPort(),
                    path,
                    null,
                    null).toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid Home Assistant WebSocket URL", e);
        }
    }

    private final class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            // Home Assistant speaks first with auth_required.
        }

        @Override
        public void onMessage(WebSocket socket, String text) {
            final JSONObject message;
            try {
                message = new JSONObject(text);
            } catch (JSONException invalid) {
                notifyOffline("Home Assistant sent an unreadable message.");
                socket.close(1002, "Invalid Home Assistant message");
                return;
            }

            String type = message.optString("type", "");
            if ("auth_required".equals(type)) {
                sendAuthentication(socket);
                return;
            }
            if ("auth_ok".equals(type)) {
                Listener target;
                synchronized (lock) {
                    if (socket != webSocket || deliberateClose) {
                        return;
                    }
                    ready = true;
                    target = listener;
                }
                if (target != null) {
                    target.onReady();
                }
                return;
            }
            if ("auth_invalid".equals(type)) {
                Listener target;
                synchronized (lock) {
                    if (socket != webSocket || deliberateClose) {
                        return;
                    }
                    ready = false;
                    target = listener;
                }
                if (target != null) {
                    target.onReauthRequired(message.optString(
                            "message",
                            "Home Assistant needs BOOP to pair again."));
                }
                socket.close(1000, "Home Assistant authentication rejected");
                return;
            }
            if ("event".equals(type)) {
                handleStateChangeEvent(message);
                handleAutomationTriggerEvent(message);
                return;
            }

            if (message.has("id")) {
                handleCommandResult(message);
            }
        }

        @Override
        public void onClosed(WebSocket socket, int code, String reason) {
            if (!isDeliberatelyClosed(socket)) {
                notifyOffline("Home Assistant disconnected.");
            }
        }

        @Override
        public void onFailure(WebSocket socket, Throwable throwable, Response response) {
            if (!isDeliberatelyClosed(socket)) {
                notifyOffline("I can't reach Home Assistant right now.");
            }
        }
    }

    private void sendAuthentication(WebSocket socket) {
        final String token;
        synchronized (lock) {
            if (socket != webSocket || deliberateClose) {
                return;
            }
            token = accessToken;
        }
        if (token == null) {
            return;
        }
        try {
            JSONObject auth = new JSONObject();
            auth.put("type", "auth");
            auth.put("access_token", token);
            socket.send(auth.toString());
        } catch (JSONException impossible) {
            notifyOffline("BOOP couldn't prepare Home Assistant authentication.");
        }
    }

    private void handleCommandResult(JSONObject message) {
        int id = message.optInt("id", -1);
        if (id < 1) {
            return;
        }
        Callback callback;
        synchronized (lock) {
            callback = callbacks.remove(id);
        }
        if (callback == null) {
            return;
        }

        boolean success = message.optBoolean("success", false);
        Object result = message.opt("result");
        if (result == JSONObject.NULL) {
            result = null;
        }
        String error = null;
        if (!success) {
            Object rawError = message.opt("error");
            if (rawError instanceof JSONObject) {
                JSONObject errorObject = (JSONObject) rawError;
                error = errorObject.optString("message", errorObject.toString());
            } else if (rawError != null && rawError != JSONObject.NULL) {
                error = String.valueOf(rawError);
            } else {
                error = "Home Assistant rejected the request";
            }
        }
        callback.onResult(success, result, error);
    }

    private void handleStateChangeEvent(JSONObject message) {
        int subscriptionId = message.optInt("id", -1);
        if (subscriptionId < 1) {
            return;
        }

        final StateChangeListener target;
        synchronized (lock) {
            target = stateChangeSubscriptions.get(subscriptionId);
        }
        if (target == null) {
            return;
        }

        JSONObject event = message.optJSONObject("event");
        if (event == null || !"state_changed".equals(event.optString("event_type", ""))) {
            return;
        }
        JSONObject data = event.optJSONObject("data");
        JSONObject newState = data == null ? null : data.optJSONObject("new_state");
        String entityId = data == null ? null : clean(data.optString("entity_id", null));
        String state = newState == null ? null : clean(newState.optString("state", null));
        if (entityId == null || state == null) {
            return;
        }
        target.onStateChanged(entityId, state);
    }

    private void handleAutomationTriggerEvent(JSONObject message) {
        int subscriptionId = message.optInt("id", -1);
        if (subscriptionId < 1) {
            return;
        }

        final AutomationTriggerListener target;
        synchronized (lock) {
            target = automationTriggerSubscriptions.get(subscriptionId);
        }
        if (target == null) {
            return;
        }

        JSONObject event = message.optJSONObject("event");
        if (event == null || !"automation_triggered".equals(event.optString("event_type", ""))) {
            return;
        }
        JSONObject data = event.optJSONObject("data");
        String entityId = data == null ? null : clean(data.optString("entity_id", null));
        if (entityId != null) {
            target.onTriggered(entityId);
        }
    }

    private boolean isDeliberatelyClosed(WebSocket socket) {
        synchronized (lock) {
            return deliberateClose || socket != webSocket;
        }
    }

    private void notifyOffline(String message) {
        final Listener target;
        final Map<Integer, Callback> pending;
        synchronized (lock) {
            if (deliberateClose) {
                return;
            }
            ready = false;
            target = listener;
            pending = new HashMap<>(callbacks);
            callbacks.clear();
            stateChangeSubscriptions.clear();
            automationTriggerSubscriptions.clear();
        }
        for (Callback callback : pending.values()) {
            callback.onResult(false, null, message);
        }
        if (target != null) {
            target.onOffline(message);
        }
    }

    private final class ActiveSubscription implements Subscription {
        private final int subscriptionId;
        private boolean cancelled;

        ActiveSubscription(int subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        @Override
        public void cancel() {
            synchronized (lock) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                stateChangeSubscriptions.remove(subscriptionId);
                automationTriggerSubscriptions.remove(subscriptionId);
                if (!ready || webSocket == null || deliberateClose) {
                    return;
                }
            }

            try {
                send(
                        "unsubscribe_events",
                        new JSONObject().put("subscription", subscriptionId),
                        null);
            } catch (JSONException | IllegalStateException ignored) {
                // Cancellation is best-effort; the local listener is already gone.
            }
        }
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}

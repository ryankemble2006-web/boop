package com.boop.alpha1;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import okhttp3.*;

final class HomeAssistantEntityDiscoveryClient {
    private static final int TIMEOUT_MS = 5000;

    Set<String> allowedEntityIds(String baseUrl, String accessToken) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Set<String>> allowed = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS).build();
        WebSocket socket = client.newWebSocket(new Request.Builder()
                .url(HomeAssistantDeviceSetupProtocol.websocketUrl(baseUrl)).build(),
                new WebSocketListener() {
                    JSONObject registry;
                    @Override public void onMessage(WebSocket ws, String text) {
                        try {
                            JSONObject message = new JSONObject(text);
                            String type = message.optString("type", "");
                            if ("auth_required".equals(type)) {
                                ws.send(HomeAssistantDeviceSetupProtocol.websocketAuthMessage(accessToken).toString());
                            } else if ("auth_invalid".equals(type)) {
                                finishFailure(new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired"));
                            } else if ("auth_ok".equals(type)) {
                                ws.send(HomeAssistantEntityDiscoveryProtocol.registryCommand(1).toString());
                            } else if ("result".equals(type) && message.optInt("id") == 1) {
                                if (!message.optBoolean("success", false)) { finishFailure(new Exception("entity registry unavailable")); return; }
                                registry = message.optJSONObject("result");
                                ws.send(HomeAssistantEntityDiscoveryProtocol.exposureCommand(2).toString());
                            } else if ("result".equals(type) && message.optInt("id") == 2) {
                                if (!message.optBoolean("success", false)) { finishFailure(new Exception("exposure registry unavailable")); return; }
                                allowed.set(intersect(registry, message.optJSONObject("result")));
                                done.countDown();
                            }
                        } catch (Exception e) { finishFailure(e); }
                    }
                    @Override public void onFailure(WebSocket ws, Throwable t, Response response) {
                        finishFailure(new Exception("Home Assistant discovery websocket failed", t));
                    }
                    private void finishFailure(Exception e) { if (failure.compareAndSet(null, e)) done.countDown(); }
                });
        try {
            if (!done.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)) throw new Exception("Home Assistant discovery timed out");
            if (failure.get() != null) throw failure.get();
            return allowed.get() == null ? Collections.emptySet() : allowed.get();
        } finally {
            socket.close(1000, "BOOP discovery complete");
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }

    private static Set<String> intersect(JSONObject registry, JSONObject exposure) {
        Set<String> result = new HashSet<>();
        JSONArray entities = registry == null ? null : registry.optJSONArray("entities");
        if (entities == null) return result;
        for (int i = 0; i < entities.length(); i++) {
            JSONObject entry = entities.optJSONObject(i);
            String id = entry == null ? "" : entry.optString("ei", "");
            if (HomeAssistantEntityDiscoveryProtocol.isVisible(entry)
                    && HomeAssistantEntityDiscoveryProtocol.isExplicitlyExposed(exposure, id)) result.add(id);
        }
        return result;
    }
}

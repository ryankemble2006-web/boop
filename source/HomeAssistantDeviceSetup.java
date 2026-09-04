package com.boop.alpha1;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

final class HomeAssistantDeviceSetup {
    enum SetupResult {
        READY,
        AUTH_REQUIRED,
        UNREACHABLE,
        AREA_NOT_FOUND,
        FORBIDDEN,
        FAILED
    }

    private static final int TIMEOUT_MS = 5000;
    private static final long WEBHOOK_READY_TIMEOUT_MS = 5000L;
    private static final long WEBHOOK_RETRY_MS = 250L;
    private static final String HOME_AREA = "Living Room";
    private static final String AREA_LIST_OPERATION = "config/area_registry/list";
    private static final String DEVICE_UPDATE_OPERATION = "config/device_registry/update";

    private final SecureTokenStore tokenStore;
    private final HomeAssistantAuth auth;

    HomeAssistantDeviceSetup(SecureTokenStore tokenStore, HomeAssistantAuth auth) {
        this.tokenStore = tokenStore;
        this.auth = auth;
    }

    SetupResult ensureReady() {
        String baseUrl = tokenStore.getBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            return SetupResult.AUTH_REQUIRED;
        }

        if (tokenStore.hasHaDeviceIdentity()) {
            return SetupResult.READY;
        }

        try {
            String accessToken = auth.freshAccessToken();
            String webhookId = tokenStore.getHaWebhookId();
            if (webhookId == null || webhookId.isEmpty()) {
                RegistrationResult registration = register(baseUrl, accessToken);
                if (registration.result != SetupResult.READY) {
                    return registration.result;
                }
                webhookId = registration.webhookId;
                tokenStore.saveHaWebhookId(webhookId);
            }

            DeviceLookup lookup = waitForHaDeviceId(baseUrl, webhookId);
            if (lookup.result != SetupResult.READY) {
                return lookup.result;
            }

            return assignLivingRoom(baseUrl, accessToken, lookup.deviceId);
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            return SetupResult.AUTH_REQUIRED;
        } catch (IOException e) {
            return SetupResult.UNREACHABLE;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return SetupResult.UNREACHABLE;
        } catch (Exception e) {
            return SetupResult.FAILED;
        }
    }

    private RegistrationResult register(String baseUrl, String accessToken) throws Exception {
        JSONObject body = HomeAssistantDeviceSetupProtocol.registrationBody(
                tokenStore.getOrCreateBoopRegistrationId(),
                nonEmpty(Build.MANUFACTURER),
                nonEmpty(Build.MODEL),
                nonEmpty(Build.VERSION.RELEASE));
        HttpJsonResponse response = postJson(
                baseUrl + "/api/mobile_app/registrations",
                accessToken,
                body);
        if (response.status == 401 || response.status == 403) {
            return new RegistrationResult(SetupResult.AUTH_REQUIRED, null);
        }
        if (response.status != 201) {
            return new RegistrationResult(SetupResult.FAILED, null);
        }
        String webhookId = HomeAssistantDeviceSetupProtocol.parseWebhookId(response.body);
        return new RegistrationResult(SetupResult.READY, webhookId);
    }

    private DeviceLookup waitForHaDeviceId(String baseUrl, String webhookId)
            throws Exception {
        long deadline = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(WEBHOOK_READY_TIMEOUT_MS);
        while (System.nanoTime() < deadline) {
            HttpJsonResponse response = postJson(
                    baseUrl + "/api/webhook/" + webhookId,
                    null,
                    HomeAssistantDeviceSetupProtocol.getConfigWebhookBody());
            if (response.status == 200) {
                String deviceId = response.body.optString("hass_device_id", "").trim();
                if (!deviceId.isEmpty()) {
                    return new DeviceLookup(SetupResult.READY, deviceId);
                }
            } else if (response.status == 410) {
                return new DeviceLookup(SetupResult.FAILED, null);
            } else if (response.status != 404 && response.status < 500) {
                return new DeviceLookup(SetupResult.FAILED, null);
            }
            Thread.sleep(WEBHOOK_RETRY_MS);
        }
        return new DeviceLookup(SetupResult.UNREACHABLE, null);
    }

    private SetupResult assignLivingRoom(
            String baseUrl, String accessToken, String haDeviceId) throws InterruptedException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<SetupResult> outcome = new AtomicReference<>();
        AtomicBoolean terminal = new AtomicBoolean(false);

        SetupSocketListener listener = new SetupSocketListener(
                accessToken,
                haDeviceId,
                done,
                outcome,
                terminal);
        Request request = new Request.Builder()
                .url(HomeAssistantDeviceSetupProtocol.websocketUrl(baseUrl))
                .build();
        WebSocket socket = client.newWebSocket(request, listener);

        try {
            if (!done.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                terminal.set(true);
                outcome.compareAndSet(null, SetupResult.UNREACHABLE);
                socket.cancel();
            } else {
                socket.close(1000, "BOOP setup complete");
            }
            SetupResult result = outcome.get();
            return result == null ? SetupResult.FAILED : result;
        } finally {
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();
        }
    }

    private final class SetupSocketListener extends WebSocketListener {
        private final String accessToken;
        private final String haDeviceId;
        private final CountDownLatch done;
        private final AtomicReference<SetupResult> outcome;
        private final AtomicBoolean terminal;

        SetupSocketListener(
                String accessToken,
                String haDeviceId,
                CountDownLatch done,
                AtomicReference<SetupResult> outcome,
                AtomicBoolean terminal) {
            this.accessToken = accessToken;
            this.haDeviceId = haDeviceId;
            this.done = done;
            this.outcome = outcome;
            this.terminal = terminal;
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (terminal.get()) {
                return;
            }
            try {
                JSONObject message = new JSONObject(text);
                String type = message.optString("type", "");
                if ("auth_required".equals(type)) {
                    sendOrFail(
                            webSocket,
                            HomeAssistantDeviceSetupProtocol.websocketAuthMessage(accessToken),
                            "auth");
                    return;
                }
                if ("auth_ok".equals(type)) {
                    sendOrFail(
                            webSocket,
                            HomeAssistantDeviceSetupProtocol.areaListMessage(1),
                            AREA_LIST_OPERATION);
                    return;
                }
                if ("auth_invalid".equals(type)) {
                    finish(SetupResult.AUTH_REQUIRED);
                    return;
                }
                if (!"result".equals(type)) {
                    finish(SetupResult.FAILED);
                    return;
                }

                int id = message.optInt("id", -1);
                if (!message.optBoolean("success", false)) {
                    finish(mapWebSocketError(message.optJSONObject("error")));
                    return;
                }

                if (id == 1) {
                    JSONArray areas = message.optJSONArray("result");
                    if (areas == null) {
                        finish(SetupResult.FAILED);
                        return;
                    }
                    String areaId = HomeAssistantDeviceSetupProtocol.findAreaId(areas, HOME_AREA);
                    if (areaId == null) {
                        finish(SetupResult.AREA_NOT_FOUND);
                        return;
                    }
                    sendOrFail(
                            webSocket,
                            HomeAssistantDeviceSetupProtocol.deviceAreaUpdateMessage(
                                    2, haDeviceId, areaId),
                            DEVICE_UPDATE_OPERATION);
                    return;
                }

                if (id == 2) {
                    tokenStore.saveHaDeviceId(haDeviceId);
                    finish(SetupResult.READY);
                    return;
                }

                finish(SetupResult.FAILED);
            } catch (Exception e) {
                finish(SetupResult.FAILED);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            finish(SetupResult.UNREACHABLE);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (!terminal.get()) {
                finish(SetupResult.FAILED);
            }
        }

        private void sendOrFail(WebSocket socket, JSONObject message, String expectedType) {
            if (!expectedType.equals(message.optString("type", ""))
                    || !socket.send(message.toString())) {
                finish(SetupResult.FAILED);
            }
        }

        private void finish(SetupResult result) {
            if (terminal.compareAndSet(false, true)) {
                outcome.set(result);
                done.countDown();
            }
        }
    }

    private static SetupResult mapWebSocketError(JSONObject error) {
        if (error == null) {
            return SetupResult.FAILED;
        }
        String text = (error.optString("code", "") + " "
                + error.optString("message", "")).toLowerCase(Locale.ROOT);
        if (text.contains("unauthor")
                || text.contains("forbidden")
                || text.contains("admin")
                || text.contains("permission")) {
            return SetupResult.FORBIDDEN;
        }
        return SetupResult.FAILED;
    }

    private static HttpJsonResponse postJson(
            String url, String accessToken, JSONObject body) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            if (accessToken != null && !accessToken.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            }

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            InputStream input = status >= 200 && status < 400
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String text = input == null ? "" : readAll(input);
            JSONObject json = text.isEmpty() ? new JSONObject() : new JSONObject(text);
            return new HttpJsonResponse(status, json);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Invalid Home Assistant setup response", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readAll(InputStream input) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static String nonEmpty(String value) {
        return value == null || value.trim().isEmpty() ? "Unknown" : value.trim();
    }

    private static final class HttpJsonResponse {
        final int status;
        final JSONObject body;

        HttpJsonResponse(int status, JSONObject body) {
            this.status = status;
            this.body = body;
        }
    }

    private static final class RegistrationResult {
        final SetupResult result;
        final String webhookId;

        RegistrationResult(SetupResult result, String webhookId) {
            this.result = result;
            this.webhookId = webhookId;
        }
    }

    private static final class DeviceLookup {
        final SetupResult result;
        final String deviceId;

        DeviceLookup(SetupResult result, String deviceId) {
            this.result = result;
            this.deviceId = deviceId;
        }
    }
}

package com.boop.alpha1;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class HomeAssistantClient {
    private static final int TIMEOUT_MS = 5000;

    private final SecureTokenStore tokenStore;
    private final HomeAssistantAuth auth;
    private final String homeArea;
    private final HomeAssistantAssistantClient assistantClient;

    HomeAssistantClient(SecureTokenStore tokenStore, HomeAssistantAuth auth, String homeArea) {
        this.tokenStore = tokenStore;
        this.auth = auth;
        this.homeArea = homeArea;
        this.assistantClient = new HomeAssistantAssistantClient(tokenStore, auth);
    }

    CommandOutcome process(String text) {
        String baseUrl = tokenStore.getBaseUrl();
        if (baseUrl == null) {
            return CommandOutcome.authRequired();
        }

        String deviceId = tokenStore.getHaDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            return CommandOutcome.authRequired();
        }

        try {
            String accessToken = auth.freshAccessToken();
            HomeAssistantResponse response = postConversation(
                    baseUrl, accessToken, text, deviceId);

            switch (response.kind()) {
                case ACTION_DONE:
                    if (!response.successTargets().isEmpty() && response.failedTargets().isEmpty()) {
                        return CommandOutcome.success(response.successTargets().get(0).name());
                    }
                    if (!response.failedTargets().isEmpty()) {
                        HomeAssistantResponse.Target failed = response.failedTargets().get(0);
                        if ("entity".equals(failed.type())
                                && !failed.id().isEmpty()
                                && isUnavailable(baseUrl, accessToken, failed.id())) {
                            return CommandOutcome.targetOffline(
                                    failed.name().isEmpty() ? "device" : failed.name(),
                                    homeArea);
                        }
                    }
                    return CommandOutcome.failed();
                case NO_INTENT_MATCH:
                    return handleAssistantFallback(text, CommandOutcome.noMatch());
                case NO_VALID_TARGETS:
                    return handleAssistantFallback(text, CommandOutcome.noTarget());
                case QUERY_ANSWER:
                case FAILED_TO_HANDLE:
                case UNKNOWN_ERROR:
                default:
                    return CommandOutcome.failed();
            }
        } catch (HomeAssistantAuth.AuthRejectedException e) {
            return CommandOutcome.authRequired();
        } catch (IOException e) {
            return CommandOutcome.unreachable();
        } catch (Exception e) {
            return CommandOutcome.authRequired();
        }
    }

    private CommandOutcome handleAssistantFallback(String text, CommandOutcome localOutcome) {
        if (!AssistantFallbackPolicy.shouldAskAssistant(localOutcome.status())) {
            return localOutcome;
        }

        AssistantOutcome assistant = assistantClient.ask(text);
        switch (assistant.status()) {
            case REPLY:
                return CommandOutcome.assistantReply(assistant.speech());
            case NO_AGENT:
                return CommandOutcome.assistantNoAgent();
            case UNREACHABLE:
                return CommandOutcome.assistantUnreachable();
            case AUTH_REQUIRED:
                return CommandOutcome.authRequired();
            case FAILED:
            default:
                return CommandOutcome.assistantFailed();
        }
    }

    private static HomeAssistantResponse postConversation(
            String baseUrl, String accessToken, String text, String deviceId) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = open(baseUrl + "/api/conversation/process", "POST", accessToken);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            JSONObject body = HomeAssistantConversationRequest.build(
                    text,
                    Locale.getDefault().toLanguageTag(),
                    deviceId);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            if (status == 401 || status == 403) {
                throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            }
            if (status < 200 || status >= 300) {
                throw new IOException("Home Assistant conversation HTTP " + status);
            }
            return HomeAssistantResponseParser.parse(readAll(connection.getInputStream()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static boolean isUnavailable(
            String baseUrl, String accessToken, String entityId) throws Exception {
        HttpURLConnection connection = null;
        try {
            String encoded = URLEncoder.encode(entityId, StandardCharsets.UTF_8);
            connection = open(baseUrl + "/api/states/" + encoded, "GET", accessToken);
            int status = connection.getResponseCode();
            if (status == 401 || status == 403) {
                throw new HomeAssistantAuth.AuthRejectedException("Home Assistant authorization expired");
            }
            if (status != 200) {
                return false;
            }
            JSONObject state = new JSONObject(readAll(connection.getInputStream()));
            return "unavailable".equalsIgnoreCase(state.optString("state", ""));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection open(String url, String method, String accessToken)
            throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept", "application/json");
        return connection;
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
}

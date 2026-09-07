package com.boop.shieldoverlay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class HomeAssistantAuthClient {
    private final OkHttpClient httpClient;

    public HomeAssistantAuthClient(OkHttpClient httpClient) {
        if (httpClient == null) {
            throw new IllegalArgumentException("HTTP client is required");
        }
        this.httpClient = httpClient;
    }

    public AuthTokenSet exchangeAuthorizationCode(
            String baseUrl,
            String authorizationCode,
            String clientId) throws IOException {
        requireText(authorizationCode, "authorization code");
        requireText(clientId, "client id");

        FormBody form = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", authorizationCode)
                .add("client_id", clientId)
                .build();

        return performTokenRequest(baseUrl, form, true);
    }

    public AuthTokenSet refresh(
            String baseUrl,
            String refreshToken,
            String clientId) throws IOException {
        requireText(refreshToken, "refresh token");
        requireText(clientId, "client id");

        FormBody form = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .build();

        return performTokenRequest(baseUrl, form, false);
    }

    private AuthTokenSet performTokenRequest(
            String baseUrl,
            FormBody form,
            boolean requireRefreshToken) throws IOException {
        String endpoint = tokenEndpoint(baseUrl);
        Request request = new Request.Builder()
                .url(endpoint)
                .post(form)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(
                        "Home Assistant token request failed (" + response.code() + ")");
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Home Assistant token response was empty");
            }

            final JSONObject json;
            try {
                json = new JSONObject(body.string());
            } catch (JSONException e) {
                throw new IOException("Home Assistant token response was not valid JSON", e);
            }

            try {
                String accessToken = requiredJsonText(json, "access_token");
                String refreshToken = optionalJsonText(json, "refresh_token");
                if (requireRefreshToken && refreshToken == null) {
                    throw new IOException("Home Assistant did not return a refresh credential");
                }
                long expiresIn = json.getLong("expires_in");
                String tokenType = requiredJsonText(json, "token_type");
                return new AuthTokenSet(
                        accessToken,
                        refreshToken,
                        expiresIn,
                        tokenType);
            } catch (JSONException | IllegalArgumentException e) {
                throw new IOException("Home Assistant token response was incomplete", e);
            }
        }
    }

    private static String tokenEndpoint(String baseUrl) {
        String base = requireText(baseUrl, "Home Assistant URL").trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/auth/token";
    }

    private static String requiredJsonText(JSONObject json, String key) throws JSONException {
        String value = json.getString(key);
        if (value == null || value.trim().isEmpty()) {
            throw new JSONException("missing required field");
        }
        return value;
    }

    private static String optionalJsonText(JSONObject json, String key) {
        if (!json.has(key) || json.isNull(key)) {
            return null;
        }
        String value = json.optString(key, null);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}

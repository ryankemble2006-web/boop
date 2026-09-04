package com.boop.alpha1;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Base64;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

final class HomeAssistantAuth {
    static final class AuthRejectedException extends Exception {
        AuthRejectedException(String message) { super(message); }
    }

    private static final String PENDING_PREFS = "boop-ha-auth-pending";
    private static final String PREF_STATE = "state";
    private static final String PREF_BASE = "base_url";
    private static final int TIMEOUT_MS = 5000;

    private final SecureTokenStore tokenStore;
    private final SharedPreferences pending;
    private final SecureRandom random = new SecureRandom();

    HomeAssistantAuth(Context context, SecureTokenStore tokenStore) {
        this.tokenStore = tokenStore;
        this.pending = context.getSharedPreferences(PENDING_PREFS, Context.MODE_PRIVATE);
    }

    String begin(String baseUrl) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String state = Base64.encodeToString(
                bytes,
                Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String cleanBase = HomeAssistantAuthUrls.trim(baseUrl);
        pending.edit()
                .putString(PREF_STATE, state)
                .putString(PREF_BASE, cleanBase)
                .apply();
        return HomeAssistantAuthUrls.authorizeUrl(cleanBase, state);
    }

    void completeCallback(Uri callback) throws Exception {
        if (callback == null
                || !"boop".equals(callback.getScheme())
                || !"auth-callback".equals(callback.getHost())) {
            throw new AuthRejectedException("Invalid BOOP callback");
        }

        String expectedState = pending.getString(PREF_STATE, null);
        String baseUrl = pending.getString(PREF_BASE, null);
        String returnedState = callback.getQueryParameter("state");
        String code = callback.getQueryParameter("code");
        if (expectedState == null || baseUrl == null
                || returnedState == null || !expectedState.equals(returnedState)
                || code == null || code.isEmpty()) {
            throw new AuthRejectedException("Authorization response did not match");
        }

        JSONObject token = postForm(
                HomeAssistantAuthUrls.tokenUrl(baseUrl),
                HomeAssistantAuthUrls.authorizationCodeBody(code));
        String refreshToken = token.optString("refresh_token", "");
        if (refreshToken.isEmpty()) {
            throw new AuthRejectedException("Home Assistant did not return a refresh token");
        }

        tokenStore.saveConnection(baseUrl, refreshToken);
        pending.edit().clear().apply();
    }

    String freshAccessToken() throws Exception {
        String baseUrl = tokenStore.getBaseUrl();
        String refreshToken = tokenStore.getRefreshToken();
        if (baseUrl == null || refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthRejectedException("No saved Home Assistant connection");
        }

        JSONObject token = postForm(
                HomeAssistantAuthUrls.tokenUrl(baseUrl),
                HomeAssistantAuthUrls.refreshBody(refreshToken));
        String accessToken = token.optString("access_token", "");
        if (accessToken.isEmpty()) {
            throw new AuthRejectedException("Home Assistant did not return an access token");
        }
        return accessToken;
    }

    private static JSONObject postForm(String url, String body) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");

            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(payload.length);
            try (OutputStream out = connection.getOutputStream()) {
                out.write(payload);
            }

            int status = connection.getResponseCode();
            if (status == 400 || status == 401 || status == 403) {
                throw new AuthRejectedException("Home Assistant rejected authorization");
            }
            if (status < 200 || status >= 300) {
                throw new IOException("Home Assistant auth HTTP " + status);
            }
            return new JSONObject(readAll(connection.getInputStream()));
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
}

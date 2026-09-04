package com.boop.alpha1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class HomeAssistantAuthUrls {
    static final String CLIENT_ID = "https://ryankemble2006-web.github.io/boop/ha-auth/";
    static final String REDIRECT_URI = "https://ryankemble2006-web.github.io/boop/ha-auth/callback.html";
    static final String APP_CALLBACK = "boop://auth-callback";

    private HomeAssistantAuthUrls() { }

    static String authorizeUrl(String baseUrl, String state) {
        return trim(baseUrl) + "/auth/authorize?client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&state=" + enc(state);
    }

    static String tokenUrl(String baseUrl) {
        return trim(baseUrl) + "/auth/token";
    }

    static String authorizationCodeBody(String code) {
        return "grant_type=authorization_code&code=" + enc(code)
                + "&client_id=" + enc(CLIENT_ID);
    }

    static String refreshBody(String refreshToken) {
        return "grant_type=refresh_token&refresh_token=" + enc(refreshToken)
                + "&client_id=" + enc(CLIENT_ID);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    static String trim(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}

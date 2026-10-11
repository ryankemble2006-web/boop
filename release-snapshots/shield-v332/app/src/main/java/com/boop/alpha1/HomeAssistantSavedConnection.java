package com.boop.alpha1;

/** In-memory snapshot: keep the server and the OAuth identity with its token. */
final class HomeAssistantSavedConnection {
    private final String baseUrl, clientId, refreshToken;
    HomeAssistantSavedConnection(String baseUrl, String clientId, String refreshToken) {
        if (baseUrl == null || baseUrl.isBlank() || clientId == null || clientId.isBlank()
                || refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Incomplete saved connection");
        }
        this.baseUrl = HomeAssistantAuthUrls.trim(baseUrl);
        this.clientId = clientId;
        this.refreshToken = refreshToken;
    }
    String baseUrl() { return baseUrl; }
    String refreshToken() { return refreshToken; }
    String refreshBody() {
        return "grant_type=refresh_token&refresh_token="
                + HomeAssistantAuthUrls.enc(refreshToken)
                + "&client_id=" + HomeAssistantAuthUrls.enc(clientId);
    }
}

package com.boop.shieldoverlay;

public final class StoredHomeAssistantCredential {
    private final String baseUrl;
    private final String clientId;
    private final String refreshToken;

    public StoredHomeAssistantCredential(
            String baseUrl,
            String clientId,
            String refreshToken) {
        this.baseUrl = requireText(baseUrl, "Home Assistant URL");
        this.clientId = requireText(clientId, "client id");
        this.refreshToken = requireText(refreshToken, "refresh credential");
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String clientId() {
        return clientId;
    }

    public String refreshToken() {
        return refreshToken;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}

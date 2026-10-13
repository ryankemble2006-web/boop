package com.boop.shieldoverlay;

public final class PairingRelayMessage {
    private final String authorizationCode;
    private final String clientId;

    public PairingRelayMessage(String authorizationCode, String clientId) {
        this.authorizationCode = requireText(authorizationCode, "authorization code");
        this.clientId = requireText(clientId, "client id");
    }

    public String authorizationCode() {
        return authorizationCode;
    }

    public String clientId() {
        return clientId;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}

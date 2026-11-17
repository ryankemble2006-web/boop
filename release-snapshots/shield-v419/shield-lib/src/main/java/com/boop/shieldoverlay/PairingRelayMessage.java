package com.boop.shieldoverlay;

public final class PairingRelayMessage {
    private final String authorizationCode;
    private final String clientId;
    private final String token;

    public PairingRelayMessage(String authorizationCode, String clientId) {
        this(authorizationCode, clientId, null);
    }

    public PairingRelayMessage(String authorizationCode, String clientId, String token) {
        this.authorizationCode = authorizationCode;
        this.clientId = clientId == null ? "boop_shield" : clientId;
        this.token = token;
    }

    public String authorizationCode() {
        return authorizationCode;
    }

    public String clientId() {
        return clientId;
    }

    public String token() {
        return token;
    }
}

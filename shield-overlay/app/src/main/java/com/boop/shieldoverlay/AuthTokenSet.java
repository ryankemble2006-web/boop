package com.boop.shieldoverlay;

public final class AuthTokenSet {
    private final String accessToken;
    private final String refreshToken;
    private final long expiresInSeconds;
    private final String tokenType;

    public AuthTokenSet(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            String tokenType) {
        this.accessToken = requireText(accessToken, "access token");
        if (expiresInSeconds < 0L) {
            throw new IllegalArgumentException("expires_in must not be negative");
        }
        this.refreshToken = normalizeOptional(refreshToken);
        this.expiresInSeconds = expiresInSeconds;
        this.tokenType = requireText(tokenType, "token type");
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    public long expiresInSeconds() {
        return expiresInSeconds;
    }

    public String tokenType() {
        return tokenType;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }
}

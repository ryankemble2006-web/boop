package com.boop.shieldoverlay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PairingSession {
    private static final long LIFETIME_MS = 120_000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String sessionId;
    private final String secret;
    private final long expiresAtMs;
    private boolean consumed;

    private PairingSession(String sessionId, String secret, long expiresAtMs) {
        this.sessionId = sessionId;
        this.secret = secret;
        this.expiresAtMs = expiresAtMs;
    }

    public static PairingSession newSession(long nowMs) {
        return new PairingSession(
                randomUrlSafe(16),
                randomUrlSafe(32),
                nowMs + LIFETIME_MS);
    }

    public boolean isActive(long nowMs) {
        return !consumed && nowMs < expiresAtMs;
    }

    public synchronized boolean consume(String candidateSecret, long nowMs) {
        if (!isActive(nowMs) || candidateSecret == null) {
            return false;
        }

        byte[] expected = secret.getBytes(StandardCharsets.UTF_8);
        byte[] candidate = candidateSecret.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, candidate)) {
            return false;
        }

        consumed = true;
        return true;
    }

    public String sessionId() {
        return sessionId;
    }

    public String secret() {
        return secret;
    }

    public long expiresAtMs() {
        return expiresAtMs;
    }

    private static String randomUrlSafe(int byteCount) {
        byte[] bytes = new byte[byteCount];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

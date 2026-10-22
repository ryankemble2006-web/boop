package com.boop.alpha1;

import android.net.Uri;

import java.net.URI;

public final class PairingLink {
    private static final String SCHEME = "boop";
    private static final String HOST = "shield-pair";
    private static final String VERSION = "1";

    private final String host;
    private final int port;
    private final String sessionId;
    private final String secret;
    private final String certificatePinSha256;
    private final String homeAssistantBaseUrl;

    private PairingLink(
            String host,
            int port,
            String sessionId,
            String secret,
            String certificatePinSha256,
            String homeAssistantBaseUrl) {
        this.host = host;
        this.port = port;
        this.sessionId = sessionId;
        this.secret = secret;
        this.certificatePinSha256 = certificatePinSha256;
        this.homeAssistantBaseUrl = homeAssistantBaseUrl;
    }

    public static PairingLink parse(Uri uri) {
        if (uri == null
                || !SCHEME.equals(uri.getScheme())
                || !HOST.equals(uri.getHost())
                || !VERSION.equals(uri.getQueryParameter("v"))) {
            throw new IllegalArgumentException("Not a BOOP Shield pairing link");
        }

        String host = required(uri, "host");
        int port;
        try {
            port = Integer.parseInt(required(uri, "port"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Shield pairing port", e);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid Shield pairing port");
        }

        String homeAssistantBaseUrl = required(uri, "ha");
        URI haUri;
        try {
            haUri = URI.create(homeAssistantBaseUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Home Assistant address", e);
        }
        String scheme = haUri.getScheme();
        if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || haUri.getHost() == null) {
            throw new IllegalArgumentException("Home Assistant address must be local HTTP or HTTPS");
        }

        return new PairingLink(
                host,
                port,
                required(uri, "sid"),
                required(uri, "secret"),
                required(uri, "pin"),
                homeAssistantBaseUrl);
    }

    private static String required(Uri uri, String key) {
        String value = uri.getQueryParameter(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing pairing field: " + key);
        }
        return value;
    }

    public String host() { return host; }
    public int port() { return port; }
    public String sessionId() { return sessionId; }
    public String secret() { return secret; }
    public String certificatePinSha256() { return certificatePinSha256; }
    public String homeAssistantBaseUrl() { return homeAssistantBaseUrl; }
}

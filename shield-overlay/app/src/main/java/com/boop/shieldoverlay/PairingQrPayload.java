package com.boop.shieldoverlay;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PairingQrPayload {
    private static final String SCHEME = "boop";
    private static final String AUTHORITY = "shield-pair";
    private static final String VERSION = "1";

    private final String host;
    private final int port;
    private final String sessionId;
    private final String secret;
    private final String certificatePinSha256;
    private final String homeAssistantBaseUrl;

    public PairingQrPayload(
            String host,
            int port,
            String sessionId,
            String secret,
            String certificatePinSha256,
            String homeAssistantBaseUrl) {
        this.host = requireText(host, "host");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port out of range");
        }
        this.port = port;
        this.sessionId = requireText(sessionId, "session id");
        this.secret = requireText(secret, "secret");
        this.certificatePinSha256 = requireText(certificatePinSha256, "certificate pin");
        this.homeAssistantBaseUrl = validateHomeAssistantUrl(homeAssistantBaseUrl);
    }

    public URI toUri() {
        String query = "v=" + encode(VERSION)
                + "&host=" + encode(host)
                + "&port=" + port
                + "&sid=" + encode(sessionId)
                + "&secret=" + encode(secret)
                + "&pin=" + encode(certificatePinSha256)
                + "&ha=" + encode(homeAssistantBaseUrl);
        return URI.create(SCHEME + "://" + AUTHORITY + "?" + query);
    }

    public static PairingQrPayload parse(URI uri) {
        if (uri == null
                || !SCHEME.equals(uri.getScheme())
                || !AUTHORITY.equals(uri.getAuthority())) {
            throw new IllegalArgumentException("not a BOOP Shield pairing URI");
        }

        Map<String, String> values = parseQuery(uri.getRawQuery());
        if (!VERSION.equals(values.get("v"))) {
            throw new IllegalArgumentException("unsupported pairing version");
        }

        int port;
        try {
            port = Integer.parseInt(required(values, "port"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid port", e);
        }

        return new PairingQrPayload(
                required(values, "host"),
                port,
                required(values, "sid"),
                required(values, "secret"),
                required(values, "pin"),
                required(values, "ha"));
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String sessionId() {
        return sessionId;
    }

    public String secret() {
        return secret;
    }

    public String certificatePinSha256() {
        return certificatePinSha256;
    }

    public String homeAssistantBaseUrl() {
        return homeAssistantBaseUrl;
    }

    private static String validateHomeAssistantUrl(String value) {
        String text = requireText(value, "Home Assistant URL");
        URI uri;
        try {
            uri = URI.create(text);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid Home Assistant URL", e);
        }
        String scheme = uri.getScheme();
        if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("Home Assistant URL must be HTTP or HTTPS");
        }
        return text;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            throw new IllegalArgumentException("pairing query missing");
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String part : rawQuery.split("&")) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                throw new IllegalArgumentException("invalid pairing query");
            }
            String key = decode(part.substring(0, separator));
            String value = decode(part.substring(separator + 1));
            if (values.put(key, value) != null) {
                throw new IllegalArgumentException("duplicate pairing field");
            }
        }
        return values;
    }

    private static String required(Map<String, String> values, String key) {
        return requireText(values.get(key), key);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 unavailable", impossible);
        }
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException impossible) {
            throw new IllegalStateException("UTF-8 unavailable", impossible);
        }
    }
}

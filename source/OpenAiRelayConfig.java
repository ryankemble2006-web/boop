package com.boop.alpha1;

import java.net.URI;
import java.net.URISyntaxException;

/** Prototype relay credential only. An OpenAI/provider key must never enter Android. */
final class OpenAiRelayConfig {
    private final String url;
    private final String token;

    OpenAiRelayConfig(String url, String token) {
        this.url = url == null ? "" : url.trim();
        this.token = token == null ? "" : token.trim();
    }

    static OpenAiRelayConfig fromBuildConfig() {
        return new OpenAiRelayConfig(BuildConfig.BOOP_RELAY_URL, BuildConfig.BOOP_RELAY_TOKEN);
    }

    boolean configured() {
        if (url.isEmpty() || token.isEmpty() || token.startsWith("sk-")
                || !token.matches("[!-~]+")) return false;
        try {
            URI endpoint = new URI(url);
            return "https".equalsIgnoreCase(endpoint.getScheme())
                    && endpoint.getHost() != null
                    && !"api.openai.com".equalsIgnoreCase(endpoint.getHost())
                    && endpoint.getRawUserInfo() == null
                    && endpoint.getRawQuery() == null
                    && endpoint.getRawFragment() == null
                    && (endpoint.getPort() == -1 || (endpoint.getPort() > 0 && endpoint.getPort() <= 65535));
        } catch (URISyntaxException invalid) {
            return false;
        }
    }

    String url() { return url; }
    String token() { return token; }
}

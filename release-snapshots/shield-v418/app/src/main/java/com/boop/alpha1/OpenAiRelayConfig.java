package com.boop.alpha1;

final class OpenAiRelayConfig {
    final String url;
    final String token;

    OpenAiRelayConfig(String url, String token) {
        this.url = trim(url);
        this.token = trim(token);
    }

    static OpenAiRelayConfig fromBuildConfig() {
        return new OpenAiRelayConfig(BuildConfig.BOOP_RELAY_URL, BuildConfig.BOOP_RELAY_TOKEN);
    }

    boolean configured() {
        return !url.isEmpty() && !token.isEmpty();
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}

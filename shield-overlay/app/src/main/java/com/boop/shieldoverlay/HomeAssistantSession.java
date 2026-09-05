package com.boop.shieldoverlay;

public final class HomeAssistantSession {
    public static final class Access {
        private final String baseUrl;
        private final String accessToken;

        private Access(String baseUrl, String accessToken) {
            this.baseUrl = baseUrl;
            this.accessToken = accessToken;
        }

        public String baseUrl() {
            return baseUrl;
        }

        public String accessToken() {
            return accessToken;
        }
    }

    private final SecureCredentialStore credentialStore;
    private final HomeAssistantAuthClient authClient;
    private String inMemoryAccessToken;
    private String inMemoryBaseUrl;

    public HomeAssistantSession(
            SecureCredentialStore credentialStore,
            HomeAssistantAuthClient authClient) {
        if (credentialStore == null || authClient == null) {
            throw new IllegalArgumentException("Home Assistant session dependencies are required");
        }
        this.credentialStore = credentialStore;
        this.authClient = authClient;
    }

    public synchronized Access currentAccess() {
        if (inMemoryAccessToken == null || inMemoryBaseUrl == null) {
            return null;
        }
        return new Access(inMemoryBaseUrl, inMemoryAccessToken);
    }

    public Access ensureAccessToken() throws Exception {
        StoredHomeAssistantCredential stored = credentialStore.load();
        if (stored == null) {
            clearInMemoryAccessToken();
            throw new IllegalStateException("BOOP is not paired with Home Assistant");
        }

        AuthTokenSet tokens = authClient.refresh(
                stored.baseUrl(),
                stored.refreshToken(),
                stored.clientId());
        String accessToken = tokens.accessToken();
        if (accessToken == null || accessToken.trim().isEmpty()) {
            clearInMemoryAccessToken();
            throw new IllegalStateException("Home Assistant did not return an access token");
        }

        synchronized (this) {
            inMemoryBaseUrl = stored.baseUrl();
            inMemoryAccessToken = accessToken;
            return new Access(inMemoryBaseUrl, inMemoryAccessToken);
        }
    }

    public synchronized void clearInMemoryAccessToken() {
        inMemoryAccessToken = null;
        inMemoryBaseUrl = null;
    }
}

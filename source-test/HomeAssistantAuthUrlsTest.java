package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class HomeAssistantAuthUrlsTest {
    @Test public void authorizeUrlUsesBoopWebsiteIdentity() {
        String url = HomeAssistantAuthUrls.authorizeUrl("http://192.168.1.10:8123", "abc123");
        assertTrue(url.startsWith("http://192.168.1.10:8123/auth/authorize?"));
        assertTrue(url.contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2Fcallback.html"));
        assertTrue(url.contains("state=abc123"));
    }

    @Test public void tokenFormsReuseExactClientId() {
        assertTrue(HomeAssistantAuthUrls.authorizationCodeBody("code")
                .contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(HomeAssistantAuthUrls.refreshBody("refresh")
                .contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
    }

    @Test public void appCallbackIsStable() {
        assertEquals("boop://auth-callback", HomeAssistantAuthUrls.APP_CALLBACK);
    }
}

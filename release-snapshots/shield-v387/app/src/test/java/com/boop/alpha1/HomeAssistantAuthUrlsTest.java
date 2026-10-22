package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class HomeAssistantAuthUrlsTest {
    @Test public void authorizeUrlUsesBoopWebsiteIdentity() {
        String url = HomeAssistantAuthUrls.authorizeUrl("http://192.168.1.10:8123", "abc123");
        assertTrue(url.startsWith("http://192.168.1.10:8123/auth/authorize?"));
        assertTrue(url.contains("client_id=https%3A%2F%2Fraw.githubusercontent.com%2Fryankemble2006-web%2Fboop%2Falpha2-local-ha-control%2Fweb%2Fha-auth%2Findex.html"));
        assertTrue(url.contains("redirect_uri=boop%3A%2F%2Fauth-callback"));
        assertTrue(url.contains("state=abc123"));
    }

    @Test public void tokenFormsReuseExactClientId() {
        String encodedClientId = "client_id=https%3A%2F%2Fraw.githubusercontent.com%2Fryankemble2006-web%2Fboop%2Falpha2-local-ha-control%2Fweb%2Fha-auth%2Findex.html";
        assertTrue(HomeAssistantAuthUrls.authorizationCodeBody("code").contains(encodedClientId));
        assertTrue(HomeAssistantAuthUrls.refreshBody("refresh").contains(encodedClientId));
    }

    @Test public void appCallbackIsStable() {
        assertEquals("boop://auth-callback", HomeAssistantAuthUrls.APP_CALLBACK);
        assertEquals(HomeAssistantAuthUrls.APP_CALLBACK, HomeAssistantAuthUrls.REDIRECT_URI);
    }
}

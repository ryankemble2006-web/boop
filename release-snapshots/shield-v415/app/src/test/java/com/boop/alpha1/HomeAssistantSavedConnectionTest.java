package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.*;

public final class HomeAssistantSavedConnectionTest {
    @Test public void shieldRefreshKeepsIssuingClientAndEscapesToken() {
        HomeAssistantSavedConnection saved = new HomeAssistantSavedConnection(
                "https://house.example/", "https://shield.example/", "a+b&c");
        assertEquals("https://house.example", saved.baseUrl());
        assertEquals("grant_type=refresh_token&refresh_token=a%2Bb%26c&client_id=https%3A%2F%2Fshield.example%2F",
                saved.refreshBody());
    }

    @Test public void wallIdentityStaysUnchanged() {
        HomeAssistantSavedConnection saved = new HomeAssistantSavedConnection(
                "https://house.example", HomeAssistantAuthUrls.CLIENT_ID, "refresh");
        assertEquals(HomeAssistantAuthUrls.refreshBody("refresh"), saved.refreshBody());
    }

    @Test(expected = IllegalArgumentException.class) public void missingClientCannotUseWallFallback() {
        new HomeAssistantSavedConnection("https://house.example", "", "refresh");
    }
}

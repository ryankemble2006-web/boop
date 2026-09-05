package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public final class HomeAssistantDiscoveryTest {
    @Test
    public void internalUrlWinsOverResolvedHostAndExternalUrl() {
        Map<String, String> txt = new HashMap<>();
        txt.put("location_name", "Home");
        txt.put("uuid", "ha-uuid-1");
        txt.put("internal_url", "http://homeassistant.local:8123");
        txt.put("external_url", "https://example.duckdns.org");

        DiscoveredHomeAssistant found = HomeAssistantDiscovery.selectLocalEndpoint(
                "Home Assistant",
                "192.168.1.20",
                8123,
                txt);

        assertEquals("Home", found.name());
        assertEquals("ha-uuid-1", found.uuid());
        assertEquals("http://homeassistant.local:8123", found.baseUrl());
    }

    @Test
    public void fallsBackToResolvedHostAndPortWhenInternalUrlMissing() {
        Map<String, String> txt = new HashMap<>();
        txt.put("location_name", "Flat");
        txt.put("uuid", "ha-uuid-2");
        txt.put("external_url", "https://example.nabu.casa");

        DiscoveredHomeAssistant found = HomeAssistantDiscovery.selectLocalEndpoint(
                "Home Assistant",
                "192.168.1.30",
                8123,
                txt);

        assertEquals("Flat", found.name());
        assertEquals("http://192.168.1.30:8123", found.baseUrl());
        assertFalse(found.baseUrl().contains("nabu.casa"));
    }

    @Test
    public void blankInternalUrlNeverFallsBackToExternalUrl() {
        Map<String, String> txt = new HashMap<>();
        txt.put("internal_url", "   ");
        txt.put("external_url", "https://outside.example.com");

        DiscoveredHomeAssistant found = HomeAssistantDiscovery.selectLocalEndpoint(
                "Home Assistant",
                "10.0.0.8",
                8123,
                txt);

        assertEquals("http://10.0.0.8:8123", found.baseUrl());
        assertFalse(found.baseUrl().contains("outside.example.com"));
    }

    @Test
    public void ipv6FallbackIsBracketed() {
        DiscoveredHomeAssistant found = HomeAssistantDiscovery.selectLocalEndpoint(
                "Home Assistant",
                "fe80::1234",
                8123,
                new HashMap<>());

        assertEquals("http://[fe80::1234]:8123", found.baseUrl());
    }
}

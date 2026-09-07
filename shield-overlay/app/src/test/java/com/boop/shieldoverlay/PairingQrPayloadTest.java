package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import org.junit.Test;

public final class PairingQrPayloadTest {
    @Test
    public void payloadRoundTripsWithoutPersistentCredentials() {
        PairingQrPayload payload = new PairingQrPayload(
                "192.168.1.50",
                42123,
                "session-123",
                "one-time-secret",
                "cert-pin",
                "http://homeassistant.local:8123");

        URI uri = payload.toUri();
        String text = uri.toString();

        assertFalse(text.contains("refresh_token"));
        assertFalse(text.contains("access_token"));
        assertFalse(text.contains("password"));

        PairingQrPayload parsed = PairingQrPayload.parse(uri);
        assertEquals(payload.host(), parsed.host());
        assertEquals(payload.port(), parsed.port());
        assertEquals(payload.sessionId(), parsed.sessionId());
        assertEquals(payload.secret(), parsed.secret());
        assertEquals(payload.certificatePinSha256(), parsed.certificatePinSha256());
        assertEquals(payload.homeAssistantBaseUrl(), parsed.homeAssistantBaseUrl());
    }

    @Test
    public void rejectsWrongSchemeOrVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> PairingQrPayload.parse(URI.create("https://shield-pair?v=1")));
        assertThrows(IllegalArgumentException.class,
                () -> PairingQrPayload.parse(URI.create(
                        "boop://shield-pair?v=2&host=192.168.1.50&port=42123&sid=s&secret=x&pin=p&ha=http%3A%2F%2Fha.local%3A8123")));
    }
}

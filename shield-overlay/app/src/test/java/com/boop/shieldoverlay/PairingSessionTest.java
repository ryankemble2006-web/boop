package com.boop.shieldoverlay;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class PairingSessionTest {
    @Test
    public void sessionExpiresAtExactly120Seconds() {
        PairingSession session = PairingSession.newSession(1_000L);

        assertTrue(session.isActive(120_999L));
        assertFalse(session.isActive(121_000L));
    }

    @Test
    public void correctSecretConsumesOnlyOnce() {
        PairingSession session = PairingSession.newSession(1_000L);

        assertTrue(session.consume(session.secret(), 2_000L));
        assertFalse(session.consume(session.secret(), 3_000L));
    }

    @Test
    public void wrongSecretDoesNotConsumeSession() {
        PairingSession session = PairingSession.newSession(1_000L);

        assertFalse(session.consume("not-the-secret", 2_000L));
        assertTrue(session.consume(session.secret(), 3_000L));
    }
}

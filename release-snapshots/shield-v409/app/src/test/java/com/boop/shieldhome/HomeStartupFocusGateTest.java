package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HomeStartupFocusGateTest {
    @Test public void waitsUntilFavouritesLoadAndCanBeRequestedAgainAfterWake() {
        HomeStartupFocusGate gate = new HomeStartupFocusGate();

        assertFalse(gate.consumeWhenReady(false));
        assertTrue(gate.consumeWhenReady(true));
        assertFalse(gate.consumeWhenReady(true));

        gate.request();
        assertFalse(gate.consumeWhenReady(false));
        assertTrue(gate.consumeWhenReady(true));
        assertFalse(gate.consumeWhenReady(true));
    }
}

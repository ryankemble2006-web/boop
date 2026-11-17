package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopDockPowerTest {
    @Test public void externalPowerEnablesContinuousWakeForAcUsbWirelessAndDock() {
        assertFalse(BoopDockPower.isExternallyPowered(0));
        assertTrue(BoopDockPower.isExternallyPowered(1));
        assertTrue(BoopDockPower.isExternallyPowered(2));
        assertTrue(BoopDockPower.isExternallyPowered(4));
        assertTrue(BoopDockPower.isExternallyPowered(8));
        assertTrue(BoopDockPower.isExternallyPowered(1 | 2));
        assertFalse(BoopDockPower.isExternallyPowered(16));
    }

    @Test public void wirelessDetectionRemainsSpecificToWirelessDocking() {
        assertTrue(BoopDockPower.isWireless(BoopDockPower.PLUGGED_WIRELESS));
        assertTrue(BoopDockPower.isWireless(BoopDockPower.PLUGGED_WIRELESS | 1));
        assertFalse(BoopDockPower.isWireless(0));
        assertFalse(BoopDockPower.isWireless(1));
        assertFalse(BoopDockPower.isWireless(2));
        assertFalse(BoopDockPower.isWireless(8));
    }
}

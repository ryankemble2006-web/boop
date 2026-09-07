package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopDockPowerTest {
    @Test public void wirelessFlagEnablesDockModeWithoutTreatingUsbAsDocked() {
        assertTrue(BoopDockPower.isWireless(BoopDockPower.PLUGGED_WIRELESS));
        assertTrue(BoopDockPower.isWireless(BoopDockPower.PLUGGED_WIRELESS | 1));
        assertFalse(BoopDockPower.isWireless(0));
        assertFalse(BoopDockPower.isWireless(1));
        assertFalse(BoopDockPower.isWireless(2));
    }
}

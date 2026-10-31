package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class BoopWakeTriggerGateTest {
    @Test public void blocksDuplicatesForExactlyTheRefractoryWindow() {
        BoopWakeTriggerGate gate = new BoopWakeTriggerGate();
        assertTrue(gate.accept(10_000L));
        assertFalse(gate.accept(10_749L));
        assertTrue(gate.accept(10_750L));
    }
}

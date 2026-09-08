package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopWakeListeningCueStateTest {
    @Test
    public void recognizerOwnershipStartsAndStopsCueIdempotently() {
        BoopListeningCueState state = new BoopListeningCueState();

        assertFalse(state.isActive());
        assertTrue(state.start());
        assertTrue(state.isActive());
        assertFalse(state.start());
        assertTrue(state.stop());
        assertFalse(state.isActive());
        assertFalse(state.stop());
    }
}

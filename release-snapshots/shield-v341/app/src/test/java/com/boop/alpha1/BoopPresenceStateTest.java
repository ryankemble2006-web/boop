package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopPresenceStateTest {
    @Test public void startsIdleBlack() {
        BoopPresenceState state = new BoopPresenceState();
        assertTrue(state.isIdleBlack());
        assertEquals(BoopPresenceState.State.IDLE_BLACK, state.state());
    }

    @Test public void wakeTransitionsOnlyOnceUntilIdleAgain() {
        BoopPresenceState state = new BoopPresenceState();
        assertTrue(state.wake());
        assertEquals(BoopPresenceState.State.AWAKE, state.state());
        assertFalse(state.wake());
    }

    @Test public void idleReturnsAwakeStateToBlack() {
        BoopPresenceState state = new BoopPresenceState();
        state.wake();
        assertTrue(state.idle());
        assertTrue(state.isIdleBlack());
        assertFalse(state.idle());
    }
}

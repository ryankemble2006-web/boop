package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class BoopMemberBerryStateTest {
    @Test public void firstTwoWakesStayQuietAndThirdWakeTriggersFirstBerry() {
        BoopMemberBerryState state = new BoopMemberBerryState();

        assertEquals(BoopMemberBerryState.NONE, state.onWake());
        assertEquals(BoopMemberBerryState.NONE, state.onWake());
        assertEquals(0, state.onWake());
    }

    @Test public void berriesStayRareAndCycleThroughThreeVariants() {
        BoopMemberBerryState state = new BoopMemberBerryState();

        assertEquals(0, wakeTo(state, 3));
        assertEquals(1, wakeTo(state, 11));
        assertEquals(2, wakeTo(state, 19));
        assertEquals(0, wakeTo(state, 27));
    }

    private static int wakeTo(BoopMemberBerryState state, int targetWake) {
        int result = BoopMemberBerryState.NONE;
        for (int wake = state.wakeCount() + 1; wake <= targetWake; wake++) {
            result = state.onWake();
            if (wake != targetWake) {
                assertEquals(BoopMemberBerryState.NONE, result);
            }
        }
        return result;
    }
}
